package com.main.frotaBackEnd.controller;

import com.main.frotaBackEnd.model.DashboardDTO;
import com.main.frotaBackEnd.model.UsuarioDTO;
import com.main.frotaBackEnd.service.DashboardService;
import com.main.frotaBackEnd.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private TokenService tokenService;

    @PreAuthorize("hasAnyRole('PROPRIETARIO', 'SOCIO', 'OPERADOR')")
    @GetMapping
    public ResponseEntity<DashboardDTO> getDashboard() {
        UsuarioDTO user = (UsuarioDTO) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(dashboardService.obterDadosDashboard(user.getId_usuario(), user.getPerfil()));
    }
}
