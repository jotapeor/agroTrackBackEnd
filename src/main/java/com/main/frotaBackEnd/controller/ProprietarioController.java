package com.main.frotaBackEnd.controller;

import com.main.frotaBackEnd.model.UsuarioDTO;
import com.main.frotaBackEnd.service.ProprietarioService;
import com.main.frotaBackEnd.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/proprietario")
public class ProprietarioController {
    @Autowired
    private ProprietarioService proprietarioService;
    @Autowired
    private TokenService tokenService;

    @PostMapping("/registrar-colaborador")
    public String registrar(@RequestBody Map<String, String> dados, @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null)
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "Token ausente.");
        String token = authHeader.replace("Bearer ", "");
        if (!tokenService.validarToken(token))
            throw new ResponseStatusException(HttpStatusCode.valueOf(401), "Token inválido ou expirado.");
        UsuarioDTO solicitante = tokenService.extrairClaim(token);
        if (!"PROPRIETARIO".equals(solicitante.getPerfil()))
            throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Acesso negado.");
        proprietarioService.registrarColaborador(dados);
        return "Novo colaborador cadastrado com sucesso!";
    }
}
