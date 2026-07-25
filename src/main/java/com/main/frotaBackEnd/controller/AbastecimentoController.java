package com.main.frotaBackEnd.controller;

import com.main.frotaBackEnd.model.Abastecimento;
import com.main.frotaBackEnd.model.AbastecimentoDTO;
import com.main.frotaBackEnd.model.UsuarioDTO;
import com.main.frotaBackEnd.service.AbastecimentoService;
import com.main.frotaBackEnd.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Map;

@RestController
@RequestMapping("/api/abastecimentos")
public class AbastecimentoController {

    @Autowired
    private AbastecimentoService abastecimentoService;

    @Autowired
    private TokenService tokenService;

    @PreAuthorize("hasAnyRole('PROPRIETARIO', 'SOCIO', 'OPERADOR')")
    @PostMapping("/maquina/{idMaquina}")
    public ResponseEntity<?> registrarAbastecimento(
            @PathVariable Long idMaquina,
            @RequestBody AbastecimentoDTO dto) {

        UsuarioDTO usuario = (UsuarioDTO) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Abastecimento abastecimento = abastecimentoService.registrarAbastecimento(idMaquina, dto, usuario.getId_usuario(), usuario.getPerfil());

        return ResponseEntity.ok(Map.of(
            "message", "Abastecimento registrado com sucesso.",
            "id_abastecimento", abastecimento.getId()
        ));
    }
}
