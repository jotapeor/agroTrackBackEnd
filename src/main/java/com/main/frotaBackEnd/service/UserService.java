package com.main.frotaBackEnd.service;

import com.main.frotaBackEnd.model.UserDTO;
import com.main.frotaBackEnd.model.UserRequestDTO;
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

    public String logar(UserRequestDTO user) {
        String message = "";
        if (user.getEmail().isEmpty()) {
            message = "E-mail não preenchido";
        } else if (user.getSenha().isEmpty()) {
            message = "Senha não preenchida";
        }
        if (!message.isEmpty()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), message);
        }
        UserDTO loggedData = repository.login(user.getEmail(), user.getSenha());
        if (loggedData == null || loggedData.getId_usuario() == null) {
            // O repositório retorna objeto sem ID quando as credenciais não batem; lançamos 401
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "E-mail ou senha incorretos.");
        }
        return tokenService.gerarToken(loggedData); // Gera e retorna o token JWT assinado
    }
}