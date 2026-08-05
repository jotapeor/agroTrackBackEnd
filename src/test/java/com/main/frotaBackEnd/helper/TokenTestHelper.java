package com.main.frotaBackEnd.helper;

import com.main.frotaBackEnd.model.Usuario;
import com.main.frotaBackEnd.service.TokenService;

public class TokenTestHelper {

    public static String gerarToken(TokenService tokenService, Long id, String nome, String email, String perfil) {
        Usuario u = new Usuario();
        u.setId_usuario(id);
        u.setNome(nome);
        u.setEmail(email);
        u.setSenha("test_password");
        u.setPerfil(perfil);
        u.setAtivo(true);
        u.setPrimeiro_acesso(false);
        return tokenService.gerarToken(u);
    }
}
