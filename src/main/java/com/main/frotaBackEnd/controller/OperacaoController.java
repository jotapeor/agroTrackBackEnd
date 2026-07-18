package com.main.frotaBackEnd.controller;

import com.main.frotaBackEnd.model.RegistroOperacaoDTO;
import com.main.frotaBackEnd.model.TrocaStatusDTO;
import com.main.frotaBackEnd.model.UsuarioDTO;
import com.main.frotaBackEnd.service.OperacaoService;
import com.main.frotaBackEnd.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/operacoes")
public class OperacaoController {

    @Autowired
    private OperacaoService operacaoService;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/maquina/{idMaquina}/status")
    public ResponseEntity<?> trocarStatus(
            @PathVariable Long idMaquina,
            @RequestBody TrocaStatusDTO dto) {

        UsuarioDTO usuario = (UsuarioDTO) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        RegistroOperacaoDTO resumo = operacaoService.trocarStatus(idMaquina, dto, usuario.getId_usuario(), usuario.getPerfil());

        if (resumo != null) {
            return ResponseEntity.ok(resumo);
        } else {
            return ResponseEntity.ok(Map.of("message", "Status da máquina atualizado com sucesso."));
        }
    }

    @GetMapping("/maquina/{idMaquina}/historico")
    public ResponseEntity<List<Map<String, Object>>> listarHistorico(
            @PathVariable Long idMaquina) {

        return ResponseEntity.ok(operacaoService.listarHistoricoMaquina(idMaquina));
    }
}
