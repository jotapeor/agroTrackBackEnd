package com.main.frotaBackEnd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.frotaBackEnd.model.Abastecimento;
import com.main.frotaBackEnd.model.AbastecimentoDTO;
import com.main.frotaBackEnd.model.Usuario;
import com.main.frotaBackEnd.repository.UserRepository;
import com.main.frotaBackEnd.service.AbastecimentoService;
import com.main.frotaBackEnd.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AbastecimentoControllerTest {

    @Autowired private WebApplicationContext context;
    @Autowired TokenService tokenService;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean AbastecimentoService abastecimentoService;

    private MockMvc mockMvc;
    private String tokenOperador;
    private String tokenProprietario;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        tokenOperador = gerarToken("Carlos", "carlos@test.com", "OPERADOR");
        tokenProprietario = gerarToken("João", "joao@test.com", "PROPRIETARIO");
        Abastecimento ab = Mockito.mock(Abastecimento.class);
        when(ab.getId()).thenReturn(1L);
        when(abastecimentoService.registrarAbastecimento(anyLong(), any(), anyLong(), anyString())).thenReturn(ab);
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

    private AbastecimentoDTO criarDTO() {
        AbastecimentoDTO dto = new AbastecimentoDTO();
        dto.setLitros(new BigDecimal("50.0"));
        dto.setTipoCombustivel("Diesel");
        dto.setHodometroAtual(new BigDecimal("1100"));
        dto.setDataAbastecimento(LocalDateTime.now());
        return dto;
    }

    @Test
    void registrarAbastecimento_semToken_bloqueado() throws Exception {
        mockMvc.perform(post("/api/abastecimentos/maquina/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(criarDTO())))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void registrarAbastecimento_operador_retorna200() throws Exception {
        mockMvc.perform(post("/api/abastecimentos/maquina/1")
                .header("Authorization", "Bearer " + tokenOperador)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(criarDTO())))
            .andExpect(status().isOk());
    }

    @Test
    void registrarAbastecimento_proprietario_retorna200() throws Exception {
        mockMvc.perform(post("/api/abastecimentos/maquina/1")
                .header("Authorization", "Bearer " + tokenProprietario)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(criarDTO())))
            .andExpect(status().isOk());
    }
}
