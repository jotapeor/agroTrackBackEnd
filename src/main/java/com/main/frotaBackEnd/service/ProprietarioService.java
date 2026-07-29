package com.main.frotaBackEnd.service;

import com.main.frotaBackEnd.model.Maquina;
import com.main.frotaBackEnd.model.PerfilUsuario;
import com.main.frotaBackEnd.model.RegistroOperacao;
import com.main.frotaBackEnd.model.Usuario;
import com.main.frotaBackEnd.model.UsuarioDTO;
import com.main.frotaBackEnd.repository.MaquinaRepository;
import com.main.frotaBackEnd.repository.RegistroOperacaoRepository;
import com.main.frotaBackEnd.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private RegistroOperacaoRepository registroOperacaoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.upload.dir.usuarios:uploads/usuarios}")
    private String uploadDirUsuarios;

    public void registrarColaborador(String nome, String email, String senha, String perfil, MultipartFile foto) {
        if (nome == null || nome.trim().length() < 3)
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Insira um nome válido (mínimo de 3 letras).");
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$"))
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Insira um e-mail válido.");
        if (senha == null || senha.isBlank() || senha.length() < 6)
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "A senha deve ter no mínimo 6 caracteres.");
        if (userRepository.emailExiste(email))
            throw new ResponseStatusException(HttpStatusCode.valueOf(409), "E-mail já cadastrado");

        if (perfil == null || (!perfil.equals(PerfilUsuario.SOCIO) && !perfil.equals(PerfilUsuario.OPERADOR)))
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Perfil inválido. Use Sócio ou Operador.");

        try {
            Usuario usuario = new Usuario();
            usuario.setNome(nome.trim());
            usuario.setEmail(email.trim());
            usuario.setSenha(passwordEncoder.encode(senha));
            usuario.setPerfil(perfil);
            if (foto != null && !foto.isEmpty()) {
                usuario.setFoto_path(salvarFoto(foto));
            }
            userRepository.save(usuario);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(500), "Erro interno ao salvar o colaborador.");
        }
    }

    public void atualizarColaborador(Long id, Map<String, String> dados, UsuarioDTO solicitante) {
        Usuario usuario = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Colaborador não encontrado."));

        if (PerfilUsuario.SOCIO.equals(solicitante.getPerfil())) {
            if (!PerfilUsuario.OPERADOR.equals(usuario.getPerfil()))
                throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Você não tem permissão para editar este colaborador.");
        }

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
            if (!perfil.equals(PerfilUsuario.PROPRIETARIO) && !perfil.equals(PerfilUsuario.SOCIO) && !perfil.equals(PerfilUsuario.OPERADOR))
                throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Perfil inválido.");

            if (PerfilUsuario.SOCIO.equals(solicitante.getPerfil()) &&
                (PerfilUsuario.PROPRIETARIO.equals(perfil) || PerfilUsuario.SOCIO.equals(perfil)))
                throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Você não pode alterar o perfil para Proprietário ou Sócio.");
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
    public void vincularMaquinas(Long idUsuario, List<Long> idsMaquinas, UsuarioDTO solicitante) {
        Usuario usuario = userRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Colaborador não encontrado."));

        if (PerfilUsuario.SOCIO.equals(solicitante.getPerfil()) && !PerfilUsuario.OPERADOR.equals(usuario.getPerfil()))
            throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Você só pode vincular máquinas a operadores.");

        List<Long> idsAtualmente = usuario.getMaquinas().stream()
                .map(Maquina::getId)
                .collect(Collectors.toList());
        List<Long> novosIds = idsMaquinas != null ? idsMaquinas : List.of();
        List<Long> sendoRemovidos = idsAtualmente.stream()
                .filter(mid -> !novosIds.contains(mid))
                .collect(Collectors.toList());

        if (!sendoRemovidos.isEmpty()) {
            List<RegistroOperacao> abertas = registroOperacaoRepository
                    .buscarOperacoesAbertasPorMaquinasEUsuario(sendoRemovidos, idUsuario);
            if (!abertas.isEmpty()) {
                String nomes = abertas.stream()
                        .map(r -> r.getMaquina().getNome())
                        .distinct()
                        .collect(Collectors.joining(", "));
                throw new ResponseStatusException(HttpStatusCode.valueOf(409),
                        "Não é possível desvincular o operador de máquina(s) com operação em andamento: " + nomes);
            }
        }

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

    public void excluirColaborador(Long id) {
        Usuario usuario = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Colaborador não encontrado."));
        try {
            usuario.setAtivo(false);
            userRepository.save(usuario);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(500), "Erro interno ao desativar o colaborador.");
        }
    }

    public void reativarColaborador(Long id) {
        Usuario usuario = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Colaborador não encontrado."));
        try {
            usuario.setAtivo(true);
            userRepository.save(usuario);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(500), "Erro interno ao reativar o colaborador.");
        }
    }

    private String salvarFoto(MultipartFile foto) {
        try {
            String nomeArquivo = UUID.randomUUID() + "_" + foto.getOriginalFilename();
            Path caminho = Paths.get(uploadDirUsuarios).toAbsolutePath().normalize();
            Files.createDirectories(caminho);
            Path destino = caminho.resolve(nomeArquivo);
            Files.copy(foto.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            return nomeArquivo;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(500), "Erro ao salvar a foto.");
        }
    }
}
