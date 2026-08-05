package com.main.frotaBackEnd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.frotaBackEnd.model.Usuario;
import com.main.frotaBackEnd.repository.UserRepository;
import com.main.frotaBackEnd.service.TokenService;
import com.main.frotaBackEnd.service.UserService;
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

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    @Autowired private WebApplicationContext context;
    @Autowired TokenService tokenService;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean UserService userService;

    private MockMvc mockMvc;

    private String tokenProprietario;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        Usuario u = new Usuario();
        u.setNome("João");
        u.setEmail("joao@test.com");
        u.setSenha(passwordEncoder.encode("senha123"));
        u.setPerfil("PROPRIETARIO");
        u.setAtivo(true);
        u.setPrimeiro_acesso(false);
        u = userRepository.save(u);
        tokenProprietario = tokenService.gerarToken(u);
    }

    @Test
    void verificarEmail_emailNaoCadastrado_retornaDisponivel() throws Exception {
        when(userService.emailExiste("novo@test.com")).thenReturn(false);
        mockMvc.perform(get("/api/autenticar/verificar-email").param("email", "novo@test.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.disponivel").value(true));
    }

    @Test
    void alterarSenha_semToken_bloqueado() throws Exception {
        mockMvc.perform(post("/api/autenticar/alterar-senha")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("senha", "nova123"))))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void alterarSenha_senhaValida_retornaOk() throws Exception {
        doNothing().when(userService).alterarSenha(anyLong(), anyString());
        mockMvc.perform(post("/api/autenticar/alterar-senha")
                .header("Authorization", "Bearer " + tokenProprietario)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("senha", "novaSenha123"))))
            .andExpect(status().isOk());
    }

    @Test
    void alterarSenha_senhaCurta_retorna400() throws Exception {
        mockMvc.perform(post("/api/autenticar/alterar-senha")
                .header("Authorization", "Bearer " + tokenProprietario)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("senha", "abc"))))
            .andExpect(status().isBadRequest());
    }

    @Test
    void meusDados_semToken_bloqueado() throws Exception {
        mockMvc.perform(get("/api/usuario/me"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void meusDados_comToken_retornaUsuario() throws Exception {
        mockMvc.perform(get("/api/usuario/me")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("joao@test.com"));
    }

    @Test
    void emailDisponivel_semToken_bloqueado() throws Exception {
        mockMvc.perform(get("/api/usuarios/email-disponivel").param("email", "x@test.com"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void emailDisponivel_comToken_emailNaoUsado_retornaDisponivel() throws Exception {
        mockMvc.perform(get("/api/usuarios/email-disponivel")
                .header("Authorization", "Bearer " + tokenProprietario)
                .param("email", "naoexiste@test.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.disponivel").value(true));
    }
}
