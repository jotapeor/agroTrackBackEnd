package com.main.frotaBackEnd.controller;

import com.main.frotaBackEnd.model.UsuarioDTO;
import com.main.frotaBackEnd.service.TokenService;
import com.main.frotaBackEnd.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/autenticar")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private TokenService tokenService;

    @PostMapping("/logar")
    public String logar(@RequestBody Map<String, String> c) {
        return userService.logar(c.get("email"), c.get("senha"));
    }

    @GetMapping("/verificar-email")
    public Map<String, Boolean> verificarEmail(@RequestParam("email") String email) {
        return Map.of("disponivel", !userService.emailExiste(email));
    }

    @PostMapping("/alterar-senha")
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
}
