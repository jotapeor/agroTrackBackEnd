package com.main.frotaBackEnd.service;

import com.main.frotaBackEnd.model.Usuario;
import com.main.frotaBackEnd.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class ProprietarioService {
    @Autowired
    private UserRepository repository;

    public void registrarColaborador(Map<String, String> dados) {
        String nome = dados.get("nome");
        String email = dados.get("email");
        String senha = dados.get("senha");
        String perfil = dados.getOrDefault("perfil", "OPERADOR");

        if (nome == null || nome.trim().length() < 3)
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Insira um nome válido (mínimo de 3 letras).");
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$"))
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Insira um e-mail válido.");
        if (senha == null || senha.isBlank())
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "A senha não pode ser vazia.");
        if (repository.emailExiste(email))
            throw new ResponseStatusException(HttpStatusCode.valueOf(409), "E-mail já cadastrado");

        try {
            Usuario usuario = new Usuario();
            usuario.setNome(nome.trim());
            usuario.setEmail(email.trim());
            usuario.setSenha(senha);
            usuario.setPerfil(perfil);
            repository.save(usuario);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(500), "Erro interno ao salvar o colaborador.");
        }
    }
}
