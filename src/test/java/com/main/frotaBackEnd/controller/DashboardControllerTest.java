package com.main.frotaBackEnd.controller;

import com.main.frotaBackEnd.model.DashboardDTO;
import com.main.frotaBackEnd.model.Usuario;
import com.main.frotaBackEnd.repository.UserRepository;
import com.main.frotaBackEnd.service.DashboardService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DashboardControllerTest {

    @Autowired private WebApplicationContext context;
    @Autowired TokenService tokenService;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @MockitoBean DashboardService dashboardService;

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
        when(dashboardService.obterDadosDashboard(anyLong(), anyString())).thenReturn(new DashboardDTO());
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
    void dashboard_proprietario_retorna200() throws Exception {
        mockMvc.perform(get("/api/dashboard")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isOk());
    }

    @Test
    void dashboard_socio_retorna200() throws Exception {
        mockMvc.perform(get("/api/dashboard")
                .header("Authorization", "Bearer " + tokenSocio))
            .andExpect(status().isOk());
    }

    @Test
    void dashboard_operador_retorna200() throws Exception {
        mockMvc.perform(get("/api/dashboard")
                .header("Authorization", "Bearer " + tokenOperador))
            .andExpect(status().isOk());
    }

    @Test
    void dashboard_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
            .andExpect(status().is4xxClientError());
    }
}
