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

    public String logar(String email, String senha) {
        if (email == null || email.isBlank())
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "E-mail não preenchido");
        if (senha == null || senha.isBlank())
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Senha não preenchida");
        Usuario user = repository.login(email, senha);
        if (user == null) throw new ResponseStatusException(HttpStatusCode.valueOf(401), "E-mail ou senha incorretos.");
        return tokenService.gerarToken(user);
    }

    public boolean emailExiste(String email) {
        return repository.emailExiste(email);
    }

    public void alterarSenha(Long idUsuario, String novaSenha) {
        if (repository.alterarSenha(novaSenha, idUsuario) == 0) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(404), "Usuário não encontrado.");
        }
    }
}
