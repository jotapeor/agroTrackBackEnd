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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RelatorioControllerCompletoTest {

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
    void horasKm_proprietario_retorna200() throws Exception {
        mockMvc.perform(get("/api/relatorios/horas-km")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isOk());
    }

    @Test
    void horasKm_socio_retorna200() throws Exception {
        mockMvc.perform(get("/api/relatorios/horas-km")
                .header("Authorization", "Bearer " + tokenSocio))
            .andExpect(status().isOk());
    }

    @Test
    void horasKm_operador_retorna403() throws Exception {
        mockMvc.perform(get("/api/relatorios/horas-km")
                .header("Authorization", "Bearer " + tokenOperador))
            .andExpect(status().isForbidden());
    }

    @Test
    void horasKm_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/relatorios/horas-km"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void alertasTimeline_proprietario_retorna200() throws Exception {
        mockMvc.perform(get("/api/relatorios/alertas-timeline")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isOk());
    }

    @Test
    void alertasTimeline_socio_retorna200() throws Exception {
        mockMvc.perform(get("/api/relatorios/alertas-timeline")
                .header("Authorization", "Bearer " + tokenSocio))
            .andExpect(status().isOk());
    }

    @Test
    void alertasTimeline_operador_retorna403() throws Exception {
        mockMvc.perform(get("/api/relatorios/alertas-timeline")
                .header("Authorization", "Bearer " + tokenOperador))
            .andExpect(status().isForbidden());
    }

    @Test
    void alertasTimeline_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/relatorios/alertas-timeline"))
            .andExpect(status().is4xxClientError());
    }
}
