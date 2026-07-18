package com.main.frotaBackEnd.controller;

import com.main.frotaBackEnd.model.NotificacaoDTO;
import com.main.frotaBackEnd.model.UsuarioDTO;
import com.main.frotaBackEnd.service.NotificacaoService;
import com.main.frotaBackEnd.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private TokenService tokenService;

    // Removed validarToken

    @GetMapping
    public ResponseEntity<List<NotificacaoDTO>> listar() {
        UsuarioDTO user = (UsuarioDTO) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(notificacaoService.listarPorUsuario(user.getId_usuario()));
    }

    @PostMapping("/{id}/lida")
    public ResponseEntity<Map<String, String>> marcarLida(@PathVariable Long id) {
        UsuarioDTO user = (UsuarioDTO) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        notificacaoService.marcarComoLida(id, user.getId_usuario());
        return ResponseEntity.ok(Map.of("message", "Notificação marcada como lida."));
    }
}
