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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MaquinaControllerTest {

    @Autowired private WebApplicationContext context;
    @Autowired TokenService tokenService;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean MaquinaService maquinaService;
    @MockitoBean HistoricoMaquinaService historicoMaquinaService;

    private MockMvc mockMvc;
    private String tokenProprietario;
    private String tokenOperador;
    private String tokenSocio;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        tokenProprietario = gerarToken("João", "joao@test.com", "PROPRIETARIO");
        tokenOperador = gerarToken("Carlos", "carlos@test.com", "OPERADOR");
        tokenSocio = gerarToken("Ana", "ana@test.com", "SOCIO");
        when(maquinaService.listarTodas()).thenReturn(List.of());
        when(maquinaService.listarPorUsuario(anyLong())).thenReturn(List.of());
        when(maquinaService.listarArquivadas()).thenReturn(List.of());
        doNothing().when(maquinaService).cadastrar(any(), any(), any());
        doNothing().when(maquinaService).excluir(anyLong());
        doNothing().when(maquinaService).reativar(anyLong());
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
    void listar_semToken_bloqueado() throws Exception {
        mockMvc.perform(get("/api/proprietario/maquinas"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void listar_operador_retorna200() throws Exception {
        mockMvc.perform(get("/api/proprietario/maquinas")
                .header("Authorization", "Bearer " + tokenOperador))
            .andExpect(status().isOk());
    }

    @Test
    void listar_proprietario_retorna200() throws Exception {
        mockMvc.perform(get("/api/proprietario/maquinas")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isOk());
    }

    @Test
    void cadastrar_proprietario_retorna200() throws Exception {
        mockMvc.perform(multipart("/api/proprietario/maquinas")
                .param("nome", "Trator Test")
                .param("tipo", "Trator")
                .param("modelo", "Modelo X")
                .param("ano", "2022")
                .param("hodometro_inicial", "1000")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isOk());
    }

    @Test
    void cadastrar_operador_retorna403() throws Exception {
        mockMvc.perform(multipart("/api/proprietario/maquinas")
                .param("nome", "Trator Test")
                .param("tipo", "Trator")
                .param("modelo", "Modelo X")
                .param("ano", "2022")
                .param("hodometro_inicial", "1000")
                .header("Authorization", "Bearer " + tokenOperador))
            .andExpect(status().isForbidden());
    }

    @Test
    void cadastrar_socio_retorna403() throws Exception {
        mockMvc.perform(multipart("/api/proprietario/maquinas")
                .param("nome", "Trator Test")
                .param("tipo", "Trator")
                .param("modelo", "Modelo X")
                .param("ano", "2022")
                .param("hodometro_inicial", "1000")
                .header("Authorization", "Bearer " + tokenSocio))
            .andExpect(status().isForbidden());
    }

    @Test
    void excluir_proprietario_retorna200() throws Exception {
        mockMvc.perform(delete("/api/proprietario/maquinas/1")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isOk());
    }

    @Test
    void excluir_operador_retorna403() throws Exception {
        mockMvc.perform(delete("/api/proprietario/maquinas/1")
                .header("Authorization", "Bearer " + tokenOperador))
            .andExpect(status().isForbidden());
    }

    @Test
    void listarArquivadas_proprietario_retorna200() throws Exception {
        mockMvc.perform(get("/api/proprietario/maquinas/arquivadas")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isOk());
    }

    @Test
    void listarArquivadas_operador_retorna403() throws Exception {
        mockMvc.perform(get("/api/proprietario/maquinas/arquivadas")
                .header("Authorization", "Bearer " + tokenOperador))
            .andExpect(status().isForbidden());
    }

    @Test
    void reativar_proprietario_retorna200() throws Exception {
        mockMvc.perform(post("/api/proprietario/maquinas/1/reativar")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isOk());
    }
}
