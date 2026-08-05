package com.main.frotaBackEnd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.frotaBackEnd.model.MaquinaDTO;
import com.main.frotaBackEnd.model.Usuario;
import com.main.frotaBackEnd.repository.UserRepository;
import com.main.frotaBackEnd.service.HistoricoMaquinaService;
import com.main.frotaBackEnd.service.MaquinaService;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MaquinaControllerCompletoTest {

    @Autowired private WebApplicationContext context;
    @Autowired TokenService tokenService;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean MaquinaService maquinaService;
    @MockitoBean HistoricoMaquinaService historicoMaquinaService;

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

        when(maquinaService.buscarPorId(anyLong())).thenReturn(new MaquinaDTO());
        doNothing().when(maquinaService).atualizar(anyLong(), any(), any(), any());
        doNothing().when(maquinaService).autorizarRisco(anyLong(), anyString(), anyLong());
        when(historicoMaquinaService.obterHistoricoCompleto(anyLong())).thenReturn(Collections.emptyList());
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
    void buscarPorId_comToken_retorna200() throws Exception {
        mockMvc.perform(get("/api/proprietario/maquinas/1")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isOk());
    }

    @Test
    void buscarPorId_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/proprietario/maquinas/1"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void listarFazendas_autenticado_retorna200() throws Exception {
        mockMvc.perform(get("/api/proprietario/maquinas/fazendas")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isOk());
    }

    @Test
    void listarFazendas_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/proprietario/maquinas/fazendas"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void listarTalhoes_autenticado_retorna200() throws Exception {
        mockMvc.perform(get("/api/proprietario/maquinas/talhoes")
                .header("Authorization", "Bearer " + tokenSocio))
            .andExpect(status().isOk());
    }

    @Test
    void listarTalhoes_comIdFazenda_retorna200() throws Exception {
        mockMvc.perform(get("/api/proprietario/maquinas/talhoes")
                .param("id_fazenda", "1")
                .header("Authorization", "Bearer " + tokenSocio))
            .andExpect(status().isOk());
    }

    @Test
    void autorizarRisco_proprietario_retorna200() throws Exception {
        mockMvc.perform(post("/api/proprietario/maquinas/1/autorizar-risco")
                .header("Authorization", "Bearer " + tokenProprietario)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("justificativa", "Urgência de colheita."))))
            .andExpect(status().isOk());
    }

    @Test
    void autorizarRisco_operador_retorna403() throws Exception {
        mockMvc.perform(post("/api/proprietario/maquinas/1/autorizar-risco")
                .header("Authorization", "Bearer " + tokenOperador)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("justificativa", "Teste."))))
            .andExpect(status().isForbidden());
    }

    @Test
    void autorizarRisco_semJustificativa_retorna400() throws Exception {
        mockMvc.perform(post("/api/proprietario/maquinas/1/autorizar-risco")
                .header("Authorization", "Bearer " + tokenProprietario)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("justificativa", ""))))
            .andExpect(status().isBadRequest());
    }

    @Test
    void historicoCompleto_proprietario_retorna200() throws Exception {
        mockMvc.perform(get("/api/proprietario/maquinas/1/historico-completo")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isOk());
    }

    @Test
    void historicoCompleto_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/proprietario/maquinas/1/historico-completo"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void editarMaquina_proprietario_retorna200() throws Exception {
        mockMvc.perform(post("/api/proprietario/maquinas/1")
                .header("Authorization", "Bearer " + tokenProprietario)
                .param("nome", "Trator Atualizado")
                .param("tipo", "Trator")
                .param("modelo", "Modelo X")
                .param("ano", "2022")
                .param("hodometro_inicial", "1000"))
            .andExpect(status().isOk());
    }

    @Test
    void editarMaquina_operador_retorna403() throws Exception {
        mockMvc.perform(post("/api/proprietario/maquinas/1")
                .header("Authorization", "Bearer " + tokenOperador)
                .param("nome", "Trator")
                .param("tipo", "Trator")
                .param("modelo", "Modelo X")
                .param("ano", "2022")
                .param("hodometro_inicial", "1000"))
            .andExpect(status().isForbidden());
    }

    @Test
    void listarCombustiveis_maquinaInexistente_retorna404() throws Exception {
        mockMvc.perform(get("/api/proprietario/maquinas/999999/combustiveis")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isNotFound());
    }
}
