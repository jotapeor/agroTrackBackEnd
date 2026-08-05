package com.main.frotaBackEnd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.frotaBackEnd.model.AcaoOrdemDTO;
import com.main.frotaBackEnd.model.NovaOrdemDTO;
import com.main.frotaBackEnd.model.OrdemManutencaoDTO;
import com.main.frotaBackEnd.model.Usuario;
import com.main.frotaBackEnd.repository.UserRepository;
import com.main.frotaBackEnd.service.ManutencaoService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ManutencaoControllerTest {

    @Autowired private WebApplicationContext context;
    @Autowired TokenService tokenService;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean ManutencaoService manutencaoService;

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

        OrdemManutencaoDTO ordemDTO = new OrdemManutencaoDTO();
        ordemDTO.setId(1L);
        ordemDTO.setStatus("Ativa");
        when(manutencaoService.listarOrdens(anyLong(), anyString())).thenReturn(List.of());
        when(manutencaoService.abrirOrdem(anyLong(), any(), anyLong(), anyString())).thenReturn(ordemDTO);
        when(manutencaoService.aprovarOrdem(anyLong(), anyBoolean())).thenReturn(ordemDTO);
        when(manutencaoService.encerrarOrdem(anyLong(), anyString())).thenReturn(ordemDTO);
        doNothing().when(manutencaoService).removerDaAba(anyLong());
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
    void listarOrdens_semToken_bloqueado() throws Exception {
        mockMvc.perform(get("/api/manutencao/ordens"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void listarOrdens_proprietario_retorna200() throws Exception {
        mockMvc.perform(get("/api/manutencao/ordens")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isOk());
    }

    @Test
    void listarOrdens_operador_retorna200() throws Exception {
        mockMvc.perform(get("/api/manutencao/ordens")
                .header("Authorization", "Bearer " + tokenOperador))
            .andExpect(status().isOk());
    }

    @Test
    void abrirOrdem_operador_retorna200() throws Exception {
        NovaOrdemDTO dto = new NovaOrdemDTO();
        dto.setUrgencia("Baixa");
        dto.setDescricao("Manutenção preventiva");
        mockMvc.perform(post("/api/manutencao/maquina/1/ordens")
                .header("Authorization", "Bearer " + tokenOperador)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk());
    }

    @Test
    void aprovarOrdem_proprietario_retorna200() throws Exception {
        AcaoOrdemDTO dto = new AcaoOrdemDTO();
        dto.setAprovada(true);
        mockMvc.perform(post("/api/manutencao/ordens/1/aprovar")
                .header("Authorization", "Bearer " + tokenProprietario)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk());
    }

    @Test
    void aprovarOrdem_socio_retorna403() throws Exception {
        AcaoOrdemDTO dto = new AcaoOrdemDTO();
        dto.setAprovada(true);
        mockMvc.perform(post("/api/manutencao/ordens/1/aprovar")
                .header("Authorization", "Bearer " + tokenSocio)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isForbidden());
    }

    @Test
    void encerrarOrdem_proprietario_retorna200() throws Exception {
        AcaoOrdemDTO dto = new AcaoOrdemDTO();
        dto.setObservacao("Conserto finalizado com sucesso.");
        mockMvc.perform(post("/api/manutencao/ordens/1/encerrar")
                .header("Authorization", "Bearer " + tokenProprietario)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk());
    }

    @Test
    void encerrarOrdem_operador_retorna403() throws Exception {
        AcaoOrdemDTO dto = new AcaoOrdemDTO();
        dto.setObservacao("Teste");
        mockMvc.perform(post("/api/manutencao/ordens/1/encerrar")
                .header("Authorization", "Bearer " + tokenOperador)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isForbidden());
    }

    @Test
    void removerDaAba_proprietario_retorna200() throws Exception {
        mockMvc.perform(delete("/api/manutencao/ordens/1")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isOk());
    }

    @Test
    void aprovarOrdem_semCampoAprovada_retorna400() throws Exception {
        mockMvc.perform(post("/api/manutencao/ordens/1/aprovar")
                .header("Authorization", "Bearer " + tokenProprietario)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }
}
