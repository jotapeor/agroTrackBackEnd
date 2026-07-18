package com.main.frotaBackEnd.controller;

import com.main.frotaBackEnd.model.AcaoOrdemDTO;
import com.main.frotaBackEnd.model.NovaOrdemDTO;
import com.main.frotaBackEnd.model.OrdemManutencaoDTO;
import com.main.frotaBackEnd.model.UsuarioDTO;
import com.main.frotaBackEnd.service.ManutencaoService;
import com.main.frotaBackEnd.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manutencao")
public class ManutencaoController {

    @Autowired
    private ManutencaoService manutencaoService;

    // Removed validarToken and validarProprietario

    @PostMapping("/maquina/{idMaquina}/ordens")
    public ResponseEntity<OrdemManutencaoDTO> abrirOrdem(
            @PathVariable Long idMaquina,
            @RequestBody NovaOrdemDTO dto) {
        UsuarioDTO user = (UsuarioDTO) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(manutencaoService.abrirOrdem(idMaquina, dto, user.getId_usuario(), user.getPerfil()));
    }

    @PreAuthorize("hasRole('PROPRIETARIO')")
    @PostMapping("/ordens/{idOrdem}/aprovar")
    public ResponseEntity<OrdemManutencaoDTO> aprovarOrdem(
            @PathVariable Long idOrdem,
            @RequestBody AcaoOrdemDTO dto) {
        if (dto.getAprovada() == null) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Aprovação é obrigatória.");
        }
        return ResponseEntity.ok(manutencaoService.aprovarOrdem(idOrdem, dto.getAprovada()));
    }

    @PreAuthorize("hasRole('PROPRIETARIO')")
    @PostMapping("/ordens/{idOrdem}/encerrar")
    public ResponseEntity<OrdemManutencaoDTO> encerrarOrdem(
            @PathVariable Long idOrdem,
            @RequestBody AcaoOrdemDTO dto) {
        return ResponseEntity.ok(manutencaoService.encerrarOrdem(idOrdem, dto.getObservacao()));
    }

    @GetMapping("/ordens")
    public ResponseEntity<List<OrdemManutencaoDTO>> listarOrdens() {
        UsuarioDTO user = (UsuarioDTO) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(manutencaoService.listarOrdens(user.getId_usuario(), user.getPerfil()));
    }
}
