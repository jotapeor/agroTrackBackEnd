package com.main.frotaBackEnd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.frotaBackEnd.model.RegistroOperacaoDTO;
import com.main.frotaBackEnd.model.TrocaStatusDTO;
import com.main.frotaBackEnd.model.Usuario;
import com.main.frotaBackEnd.repository.UserRepository;
import com.main.frotaBackEnd.service.OperacaoService;
import com.main.frotaBackEnd.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OperacaoControllerTest {

    @Autowired private WebApplicationContext context;
    @Autowired TokenService tokenService;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean OperacaoService operacaoService;

    private MockMvc mockMvc;
    private String tokenOperador;
    private String tokenProprietario;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        tokenOperador = gerarToken("Carlos", "carlos@test.com", "OPERADOR");
        tokenProprietario = gerarToken("João", "joao@test.com", "PROPRIETARIO");
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
    void trocarStatus_semToken_bloqueado() throws Exception {
        mockMvc.perform(post("/api/operacoes/maquina/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"novoStatus\":\"Em Operacao\",\"confirmacao\":true}"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void trocarStatus_operadorAutenticado_retornaOk() throws Exception {
        when(operacaoService.trocarStatus(anyLong(), any(), anyLong(), anyString())).thenReturn(null);
        TrocaStatusDTO dto = new TrocaStatusDTO();
        dto.setNovoStatus("Em Operacao");
        dto.setConfirmacao(true);
        mockMvc.perform(post("/api/operacoes/maquina/1/status")
                .header("Authorization", "Bearer " + tokenOperador)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk());
    }

    @Test
    void listarHistorico_autenticado_retornaOk() throws Exception {
        when(operacaoService.listarHistoricoMaquina(1L)).thenReturn(List.of());
        mockMvc.perform(get("/api/operacoes/maquina/1/historico")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isOk());
    }

    @Test
    void obterOperacaoAtiva_semOperacaoAtiva_retorna404() throws Exception {
        when(operacaoService.obterOperacaoAtiva(1L)).thenReturn(null);
        mockMvc.perform(get("/api/operacoes/maquina/1/operacao-ativa")
                .header("Authorization", "Bearer " + tokenOperador))
            .andExpect(status().isNotFound());
    }

    @Test
    void obterOperacaoAtiva_comOperacaoAtiva_retornaOk() throws Exception {
        RegistroOperacaoDTO dto = new RegistroOperacaoDTO();
        dto.setId(1L);
        dto.setIdMaquina(1L);
        dto.setNomeMaquina("Trator");
        dto.setIdOperador(10L);
        dto.setNomeOperador("Carlos");
        dto.setDataInicio(LocalDateTime.now());
        when(operacaoService.obterOperacaoAtiva(1L)).thenReturn(dto);
        mockMvc.perform(get("/api/operacoes/maquina/1/operacao-ativa")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nomeMaquina").value("Trator"));
    }
}
