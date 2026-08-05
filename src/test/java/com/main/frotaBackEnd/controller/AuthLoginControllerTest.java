package com.main.frotaBackEnd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.frotaBackEnd.repository.UserRepository;
import com.main.frotaBackEnd.service.TokenService;
import com.main.frotaBackEnd.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthLoginControllerTest {

    @Autowired private WebApplicationContext context;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired TokenService tokenService;

    @MockitoBean UserService userService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void logar_credenciaisValidas_retornaToken() throws Exception {
        when(userService.logar("user@test.com", "senha123")).thenReturn("token.jwt.valido");

        mockMvc.perform(post("/api/autenticar/logar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", "user@test.com", "senha", "senha123"))))
            .andExpect(status().isOk());
    }

    @Test
    void logar_senhaErrada_retorna401() throws Exception {
        when(userService.logar(anyString(), anyString()))
            .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos."));

        mockMvc.perform(post("/api/autenticar/logar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", "user@test.com", "senha", "errada"))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void logar_emailInexistente_retorna401() throws Exception {
        when(userService.logar(eq("naoexiste@test.com"), anyString()))
            .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos."));

        mockMvc.perform(post("/api/autenticar/logar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", "naoexiste@test.com", "senha", "senha123"))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void logar_usuarioInativo_retorna403() throws Exception {
        when(userService.logar(anyString(), anyString()))
            .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Sua conta foi inativada."));

        mockMvc.perform(post("/api/autenticar/logar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", "inativo@test.com", "senha", "senha123"))))
            .andExpect(status().isForbidden());
    }
}
