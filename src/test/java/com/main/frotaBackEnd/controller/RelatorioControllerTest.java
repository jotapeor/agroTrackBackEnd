package com.main.frotaBackEnd.controller;

import com.main.frotaBackEnd.model.Usuario;
import com.main.frotaBackEnd.repository.UserRepository;
import com.main.frotaBackEnd.service.RelatorioService;
import com.main.frotaBackEnd.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RelatorioControllerTest {

    @Autowired private WebApplicationContext context;
    @Autowired TokenService tokenService;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @MockitoBean RelatorioService relatorioService;

    private MockMvc mockMvc;
    private String tokenProprietario;
    private String tokenSocio;
    private String tokenOperador;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        tokenProprietario = gerarToken("João", "joao@test.com", "PROPRIETARIO");
        tokenSocio = gerarToken("Ana", "ana@test.com", "SOCIO");
        tokenOperador = gerarToken("Carlos", "carlos@test.com", "OPERADOR");
        when(relatorioService.relatorioConsumo(any(), any())).thenReturn(List.of());
        when(relatorioService.relatorioRisco()).thenReturn(Map.of());
        when(relatorioService.relatorioOrdensPorStatus(any(), any())).thenReturn(Map.of());
        when(relatorioService.relatorioHorasKm(any(), any(), any(), any())).thenReturn(List.of());
        when(relatorioService.relatorioAlertasTimeline(any(), any())).thenReturn(List.of());
    }

    private String gerarToken(String nome, String email, String perfil) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setSenha(passwordEncoder.encode("senha123"));
        u.setPerfil(perfil);
        u.setAtivo(true);
        u.setPrimeiro_acesso(false);
        return tokenService.gerarToken(userRepository.save(u));
    }

    @Test
    void relatorioConsumo_semToken_bloqueado() throws Exception {
        mockMvc.perform(get("/api/relatorios/consumo-por-maquina"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void relatorioConsumo_operador_retorna403() throws Exception {
        mockMvc.perform(get("/api/relatorios/consumo-por-maquina")
                .header("Authorization", "Bearer " + tokenOperador))
            .andExpect(status().isForbidden());
    }

    @Test
    void relatorioConsumo_proprietario_retorna200() throws Exception {
        mockMvc.perform(get("/api/relatorios/consumo-por-maquina")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isOk());
    }

    @Test
    void relatorioConsumo_socio_retorna200() throws Exception {
        mockMvc.perform(get("/api/relatorios/consumo-por-maquina")
                .header("Authorization", "Bearer " + tokenSocio))
            .andExpect(status().isOk());
    }

    @Test
    void relatorioRisco_proprietario_retorna200() throws Exception {
        mockMvc.perform(get("/api/relatorios/risco-distribuicao")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isOk());
    }

    @Test
    void relatorioOrdensPorStatus_socio_retorna200() throws Exception {
        mockMvc.perform(get("/api/relatorios/ordens-por-status")
                .header("Authorization", "Bearer " + tokenSocio))
            .andExpect(status().isOk());
    }
}
