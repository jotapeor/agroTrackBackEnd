package com.main.frotaBackEnd.controller;

import com.main.frotaBackEnd.model.Usuario;
import com.main.frotaBackEnd.repository.UserRepository;
import com.main.frotaBackEnd.service.NotificacaoService;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificacaoControllerTest {

    @Autowired private WebApplicationContext context;
    @Autowired TokenService tokenService;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @MockitoBean NotificacaoService notificacaoService;

    private MockMvc mockMvc;
    private String tokenProprietario;
    private String tokenOperador;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        tokenProprietario = gerarToken("João", "joao@test.com", "PROPRIETARIO");
        tokenOperador = gerarToken("Carlos", "carlos@test.com", "OPERADOR");
        when(notificacaoService.listarPorUsuario(anyLong())).thenReturn(List.of());
        doNothing().when(notificacaoService).marcarComoLida(anyLong(), anyLong());
        doNothing().when(notificacaoService).remover(anyLong(), anyLong());
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
        mockMvc.perform(get("/api/notificacoes"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void listar_operadorAutenticado_retorna200() throws Exception {
        mockMvc.perform(get("/api/notificacoes")
                .header("Authorization", "Bearer " + tokenOperador))
            .andExpect(status().isOk());
    }

    @Test
    void marcarLida_proprietarioAutenticado_retorna200() throws Exception {
        mockMvc.perform(post("/api/notificacoes/1/lida")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isOk());
    }

    @Test
    void remover_operadorAutenticado_retorna200() throws Exception {
        mockMvc.perform(delete("/api/notificacoes/1")
                .header("Authorization", "Bearer " + tokenOperador))
            .andExpect(status().isOk());
    }
}
