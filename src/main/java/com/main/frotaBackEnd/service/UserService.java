package com.main.frotaBackEnd.service;

import com.main.frotaBackEnd.model.Usuario;
import com.main.frotaBackEnd.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    @Autowired
    private UserRepository repository;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public String logar(String email, String senha) {
        if (email == null || email.isBlank())
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "E-mail não preenchido");
        if (senha == null || senha.isBlank())
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Senha não preenchida");
        Usuario user = repository.login(email);
        if (user == null || !passwordEncoder.matches(senha, user.getSenha())) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "E-mail ou senha incorretos.");
        }
        if (!user.isAtivo()) throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Sua conta foi inativada. Entre em contato com o proprietário para reativá-la.");
        return tokenService.gerarToken(user);
    }

    public boolean emailExiste(String email) {
        return repository.emailExiste(email);
    }

    public void alterarSenha(Long idUsuario, String novaSenha) {
        if (repository.alterarSenha(passwordEncoder.encode(novaSenha), idUsuario) == 0) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(404), "Usuário não encontrado.");
        }
    }
}
