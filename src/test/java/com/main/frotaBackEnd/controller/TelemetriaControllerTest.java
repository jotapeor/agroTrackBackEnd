package com.main.frotaBackEnd.controller;

import com.main.frotaBackEnd.model.Maquina;
import com.main.frotaBackEnd.model.Usuario;
import com.main.frotaBackEnd.repository.MaquinaRepository;
import com.main.frotaBackEnd.repository.RegistroOperacaoRepository;
import com.main.frotaBackEnd.repository.TelemetriaMaquinaRepository;
import com.main.frotaBackEnd.repository.UserRepository;
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

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TelemetriaControllerTest {

    @Autowired private WebApplicationContext context;
    @Autowired TokenService tokenService;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @MockitoBean MaquinaRepository maquinaRepository;
    @MockitoBean TelemetriaMaquinaRepository telemetriaMaquinaRepository;
    @MockitoBean RegistroOperacaoRepository registroOperacaoRepository;

    private MockMvc mockMvc;
    private String tokenProprietario;
    private String tokenSocio;
    private String tokenOperador;
    private Maquina maquinaTest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        tokenProprietario = gerarToken("João", "joao@test.com", "PROPRIETARIO");
        tokenSocio = gerarToken("Ana", "ana@test.com", "SOCIO");
        tokenOperador = gerarToken("Carlos", "carlos@test.com", "OPERADOR");

        maquinaTest = new Maquina();
        maquinaTest.setId(1L);
        maquinaTest.setNome("Trator Test");
        maquinaTest.setStatus("Em Operacao");
        maquinaTest.setTipo("Trator");

        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquinaTest));
        when(maquinaRepository.findById(999L)).thenReturn(Optional.empty());
        when(maquinaRepository.buscarEmOperacao()).thenReturn(Collections.emptyList());
        when(registroOperacaoRepository.buscarOperacoesAtivas(anyLong())).thenReturn(Collections.emptyList());
        when(telemetriaMaquinaRepository.findTopByMaquinaIdOrderByDataAtualizacaoDesc(anyLong())).thenReturn(Optional.empty());
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
    void obterTelemetria_proprietario_retorna200() throws Exception {
        mockMvc.perform(get("/api/telemetria/maquina/1")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isOk());
    }

    @Test
    void obterTelemetria_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/telemetria/maquina/1"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void obterTelemetria_maquinaInexistente_retorna404() throws Exception {
        mockMvc.perform(get("/api/telemetria/maquina/999")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isNotFound());
    }

    @Test
    void obterTelemetria_operadorSemVinculo_retorna403() throws Exception {
        mockMvc.perform(get("/api/telemetria/maquina/1")
                .header("Authorization", "Bearer " + tokenOperador))
            .andExpect(status().isForbidden());
    }

    @Test
    void emOperacao_proprietario_retorna200() throws Exception {
        mockMvc.perform(get("/api/telemetria/em-operacao")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isOk());
    }

    @Test
    void emOperacao_operador_retorna403() throws Exception {
        mockMvc.perform(get("/api/telemetria/em-operacao")
                .header("Authorization", "Bearer " + tokenOperador))
            .andExpect(status().isForbidden());
    }
}
