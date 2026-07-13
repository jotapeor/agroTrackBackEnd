package com.main.frotaBackEnd.service;

import com.main.frotaBackEnd.model.Usuario;
import com.main.frotaBackEnd.model.UsuarioDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class TokenService {
    @Value("${api.security.token.secret}")
    private String secret;

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(this.secret));
    }

    public String gerarToken(Usuario user) {
        if (user.getId_usuario() == null || user.getNome() == null || user.getNome().isEmpty()
                || user.getEmail() == null || user.getEmail().isEmpty()
                || user.getSenha() == null || user.getSenha().isEmpty()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Credenciais inválidas.");
        }
        return Jwts.builder()
                .subject(user.getNome())
                .claim("id_usuario", user.getId_usuario())
                .claim("nome", user.getNome())
                .claim("perfil", user.getPerfil())
                .claim("primeiro_acesso", user.isPrimeiro_acesso())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86_400_000))
                .signWith(getSignKey())
                .compact();
    }

    public UsuarioDTO extrairClaim(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        UsuarioDTO user = new UsuarioDTO();
        user.setId_usuario(claims.get("id_usuario", Long.class));
        user.setNome(claims.get("nome", String.class));
        user.setPerfil(claims.get("perfil", String.class));
        user.setPrimeiro_acesso(claims.get("primeiro_acesso", Boolean.class));
        return user;
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
