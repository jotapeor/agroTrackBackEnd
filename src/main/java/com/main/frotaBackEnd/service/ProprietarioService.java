package com.main.frotaBackEnd.service;

import com.main.frotaBackEnd.model.Maquina;
import com.main.frotaBackEnd.model.Usuario;
import com.main.frotaBackEnd.repository.MaquinaRepository;
import com.main.frotaBackEnd.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProprietarioService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MaquinaRepository maquinaRepository;

    @Value("${app.upload.dir:uploads/maquinas}")
    private String uploadDir;

    public void registrarColaborador(String nome, String email, String senha, String perfil, MultipartFile foto) {
        if (nome == null || nome.trim().length() < 3)
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Insira um nome válido (mínimo de 3 letras).");
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$"))
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Insira um e-mail válido.");
        if (senha == null || senha.isBlank())
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "A senha não pode ser vazia.");
        if (userRepository.emailExiste(email))
            throw new ResponseStatusException(HttpStatusCode.valueOf(409), "E-mail já cadastrado");

        try {
            Usuario usuario = new Usuario();
            usuario.setNome(nome.trim());
            usuario.setEmail(email.trim());
            usuario.setSenha(senha);
            usuario.setPerfil(perfil);
            if (foto != null && !foto.isEmpty()) {
                usuario.setFoto_path(salvarFoto(foto));
            }
            userRepository.save(usuario);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(500), "Erro interno ao salvar o colaborador.");
        }
    }

    public void atualizarColaborador(Long id, Map<String, String> dados) {
        Usuario usuario = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Colaborador não encontrado."));

        String nome = dados.get("nome");
        String email = dados.get("email");
        String perfil = dados.get("perfil");
        String ativoStr = dados.get("ativo");

        if (nome != null && !nome.trim().isEmpty()) {
            if (nome.trim().length() < 3)
                throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Mínimo de 3 caracteres para o nome.");
            usuario.setNome(nome.trim());
        }

        if (email != null && !email.trim().isEmpty()) {
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$"))
                throw new ResponseStatusException(HttpStatusCode.valueOf(400), "E-mail inválido.");
            Usuario existing = userRepository.findByEmail(email.trim());
            if (existing != null && !existing.getId_usuario().equals(id))
                throw new ResponseStatusException(HttpStatusCode.valueOf(409), "E-mail já cadastrado para outro usuário.");
            usuario.setEmail(email.trim());
        }

        if (perfil != null && !perfil.isEmpty()) {
            if (!perfil.equals("PROPRIETARIO") && !perfil.equals("OPERADOR"))
                throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Perfil inválido.");
            usuario.setPerfil(perfil);
        }

        if (ativoStr != null) {
            usuario.setAtivo("true".equals(ativoStr));
        }

        try {
            userRepository.save(usuario);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(500), "Erro interno ao atualizar o colaborador.");
        }
    }

    @Transactional
    public void vincularMaquinas(Long idUsuario, List<Long> idsMaquinas) {
        Usuario usuario = userRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Colaborador não encontrado."));

        usuario.getMaquinas().clear();

        if (idsMaquinas != null && !idsMaquinas.isEmpty()) {
            List<Maquina> maquinas = maquinaRepository.findAllById(idsMaquinas);
            if (maquinas.size() != idsMaquinas.size())
                throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Alguma(s) máquina(s) não encontrada(s).");
            usuario.getMaquinas().addAll(maquinas);
        }

        userRepository.save(usuario);
    }

    public List<Long> listarIdsMaquinasVinculadas(Long idUsuario) {
        Usuario usuario = userRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Colaborador não encontrado."));
        return usuario.getMaquinas().stream()
                .map(Maquina::getId)
                .collect(Collectors.toList());
    }

    private String salvarFoto(MultipartFile foto) {
        try {
            String nomeArquivo = UUID.randomUUID() + "_" + foto.getOriginalFilename();
            Path caminho = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(caminho);
            Path destino = caminho.resolve(nomeArquivo);
            Files.copy(foto.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            return nomeArquivo;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(500), "Erro ao salvar a foto.");
        }
    }
}
