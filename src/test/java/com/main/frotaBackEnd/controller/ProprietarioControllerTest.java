package com.main.frotaBackEnd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.frotaBackEnd.model.Usuario;
import com.main.frotaBackEnd.repository.UserRepository;
import com.main.frotaBackEnd.service.ProprietarioService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProprietarioControllerTest {

    @Autowired private WebApplicationContext context;
    @Autowired TokenService tokenService;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean ProprietarioService proprietarioService;

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
        doNothing().when(proprietarioService).registrarColaborador(any(), any(), any(), any(), any());
        doNothing().when(proprietarioService).excluirColaborador(anyLong());
        doNothing().when(proprietarioService).reativarColaborador(anyLong());
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
    void listarColaboradores_semToken_bloqueado() throws Exception {
        mockMvc.perform(get("/api/proprietario/colaboradores"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void listarColaboradores_proprietario_retorna200() throws Exception {
        mockMvc.perform(get("/api/proprietario/colaboradores")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isOk());
    }

    @Test
    void listarColaboradores_socio_retorna200() throws Exception {
        mockMvc.perform(get("/api/proprietario/colaboradores")
                .header("Authorization", "Bearer " + tokenSocio))
            .andExpect(status().isOk());
    }

    @Test
    void listarColaboradores_operador_retorna403() throws Exception {
        mockMvc.perform(get("/api/proprietario/colaboradores")
                .header("Authorization", "Bearer " + tokenOperador))
            .andExpect(status().isForbidden());
    }

    @Test
    void registrarColaborador_proprietario_retorna200() throws Exception {
        mockMvc.perform(post("/api/proprietario/registrar-colaborador")
                .param("nome", "Novo Colaborador")
                .param("email", "novo@test.com")
                .param("senha", "senha123")
                .param("perfil", "OPERADOR")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isOk());
    }

    @Test
    void registrarColaborador_socio_retorna403() throws Exception {
        mockMvc.perform(post("/api/proprietario/registrar-colaborador")
                .param("nome", "Novo")
                .param("email", "novo2@test.com")
                .param("senha", "senha123")
                .header("Authorization", "Bearer " + tokenSocio))
            .andExpect(status().isForbidden());
    }

    @Test
    void excluirColaborador_proprietario_retorna200() throws Exception {
        mockMvc.perform(delete("/api/proprietario/colaboradores/1")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isOk());
    }

    @Test
    void excluirColaborador_socio_retorna403() throws Exception {
        mockMvc.perform(delete("/api/proprietario/colaboradores/1")
                .header("Authorization", "Bearer " + tokenSocio))
            .andExpect(status().isForbidden());
    }
}
