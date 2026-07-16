package com.main.frotaBackEnd.controller;

import com.main.frotaBackEnd.model.Usuario;
import com.main.frotaBackEnd.model.UsuarioDTO;
import com.main.frotaBackEnd.repository.UserRepository;
import com.main.frotaBackEnd.service.TokenService;
import com.main.frotaBackEnd.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserRepository userRepository;

    @Value("${app.upload.dir.usuarios:uploads/usuarios}")
    private String uploadDirUsuarios;

    @PostMapping("/autenticar/logar")
    public String logar(@RequestBody Map<String, String> c) {
        return userService.logar(c.get("email"), c.get("senha"));
    }

    @GetMapping("/autenticar/verificar-email")
    public Map<String, Boolean> verificarEmail(@RequestParam("email") String email) {
        return Map.of("disponivel", !userService.emailExiste(email));
    }

    @PostMapping("/autenticar/alterar-senha")
    public String alterarSenha(@RequestBody Map<String, String> body, @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "Token ausente.");
        String token = authHeader.replace("Bearer ", "");
        if (!tokenService.validarToken(token))
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "Token inválido ou expirado.");
        UsuarioDTO usuario = tokenService.extrairClaim(token);
        String novaSenha = body.get("senha");
        if (novaSenha == null || novaSenha.trim().isEmpty())
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "A nova senha não pode estar vazia.");
        if (novaSenha.length() < 6)
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "A senha deve ter no mínimo 6 caracteres.");
        userService.alterarSenha(usuario.getId_usuario(), novaSenha);
        return "Senha alterada com sucesso.";
    }

    @GetMapping("/usuario/me")
    public ResponseEntity<Usuario> meusDados(@RequestHeader("Authorization") String authHeader) {
        UsuarioDTO solicitante = validarQualquerPerfil(authHeader);
        Usuario usuario = userRepository.findById(solicitante.getId_usuario())
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Usuário não encontrado."));
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/usuario/me")
    public ResponseEntity<Map<String, String>> atualizarMeusDados(
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            @RequestHeader("Authorization") String authHeader) {
        UsuarioDTO solicitante = validarQualquerPerfil(authHeader);
        Usuario usuario = userRepository.findById(solicitante.getId_usuario())
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Usuário não encontrado."));

        if (nome != null && !nome.trim().isEmpty()) {
            if (nome.trim().length() < 3)
                throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Mínimo de 3 caracteres para o nome.");
            usuario.setNome(nome.trim());
        }

        if (email != null && !email.trim().isEmpty()) {
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$"))
                throw new ResponseStatusException(HttpStatusCode.valueOf(400), "E-mail inválido.");
            Usuario existing = userRepository.findByEmail(email.trim());
            if (existing != null && !existing.getId_usuario().equals(solicitante.getId_usuario()))
                throw new ResponseStatusException(HttpStatusCode.valueOf(409), "E-mail já cadastrado para outro usuário.");
            usuario.setEmail(email.trim());
        }

        if (foto != null && !foto.isEmpty()) {
            usuario.setFoto_path(salvarFotoUsuario(foto));
        }

        try {
            userRepository.save(usuario);
            // Gera novo token com dados atualizados (nome atualizado, etc.)
            String novoTicket = tokenService.gerarToken(usuario);
            return ResponseEntity.ok(Map.of("message", "Dados atualizados com sucesso!", "token", novoTicket));
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(500), "Erro interno ao atualizar os dados.");
        }
    }

    private UsuarioDTO validarQualquerPerfil(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "Token ausente.");
        String token = authHeader.replace("Bearer ", "");
        if (!tokenService.validarToken(token))
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "Token inválido ou expirado.");
        return tokenService.extrairClaim(token);
    }

    private String salvarFotoUsuario(MultipartFile foto) {
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
