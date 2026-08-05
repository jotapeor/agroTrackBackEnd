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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ColaboradorControllerCompletoTest {

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
    private Long colaboradorId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        tokenProprietario = gerarToken("João", "joao@test.com", "PROPRIETARIO");
        tokenSocio = gerarToken("Ana", "ana@test.com", "SOCIO");
        tokenOperador = gerarToken("Carlos", "carlos@test.com", "OPERADOR");

        Usuario colaborador = new Usuario();
        colaborador.setNome("Colaborador Test");
        colaborador.setEmail("colaborador@test.com");
        colaborador.setSenha(passwordEncoder.encode("senha123"));
        colaborador.setPerfil("OPERADOR");
        colaborador.setAtivo(true);
        colaborador.setPrimeiro_acesso(false);
        colaboradorId = userRepository.save(colaborador).getId_usuario();

        doNothing().when(proprietarioService).atualizarColaborador(anyLong(), anyString(), anyString(), anyString(), anyBoolean(), any(), any());
        doNothing().when(proprietarioService).vincularMaquinas(anyLong(), anyList(), any());
        doNothing().when(proprietarioService).reativarColaborador(anyLong());
        when(proprietarioService.listarIdsMaquinasVinculadas(anyLong())).thenReturn(List.of());
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
    void buscarColaborador_proprietario_retorna200() throws Exception {
        mockMvc.perform(get("/api/proprietario/colaboradores/" + colaboradorId)
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isOk());
    }

    @Test
    void buscarColaborador_operador_retorna403() throws Exception {
        mockMvc.perform(get("/api/proprietario/colaboradores/" + colaboradorId)
                .header("Authorization", "Bearer " + tokenOperador))
            .andExpect(status().isForbidden());
    }

    @Test
    void buscarColaborador_inexistente_retorna404() throws Exception {
        mockMvc.perform(get("/api/proprietario/colaboradores/999999")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isNotFound());
    }

    @Test
    void editarColaborador_proprietario_retorna200() throws Exception {
        mockMvc.perform(put("/api/proprietario/colaboradores/" + colaboradorId)
                .header("Authorization", "Bearer " + tokenProprietario)
                .param("nome", "Novo Nome")
                .param("email", "novoemail@test.com")
                .param("perfil", "OPERADOR"))
            .andExpect(status().isOk());
    }

    @Test
    void editarColaborador_operador_retorna403() throws Exception {
        mockMvc.perform(put("/api/proprietario/colaboradores/" + colaboradorId)
                .header("Authorization", "Bearer " + tokenOperador)
                .param("nome", "Novo Nome")
                .param("email", "outro@test.com")
                .param("perfil", "OPERADOR"))
            .andExpect(status().isForbidden());
    }

    @Test
    void vincularMaquinas_proprietario_retorna200() throws Exception {
        mockMvc.perform(put("/api/proprietario/colaboradores/" + colaboradorId + "/vincular-maquinas")
                .header("Authorization", "Bearer " + tokenProprietario)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(1L, 2L))))
            .andExpect(status().isOk());
    }

    @Test
    void vincularMaquinas_operador_retorna403() throws Exception {
        mockMvc.perform(put("/api/proprietario/colaboradores/" + colaboradorId + "/vincular-maquinas")
                .header("Authorization", "Bearer " + tokenOperador)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(1L))))
            .andExpect(status().isForbidden());
    }

    @Test
    void listarMaquinasVinculadas_socio_retorna200() throws Exception {
        mockMvc.perform(get("/api/proprietario/colaboradores/" + colaboradorId + "/maquinas")
                .header("Authorization", "Bearer " + tokenSocio))
            .andExpect(status().isOk());
    }

    @Test
    void listarMaquinasVinculadas_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/proprietario/colaboradores/" + colaboradorId + "/maquinas"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void reativar_proprietario_retorna200() throws Exception {
        mockMvc.perform(post("/api/proprietario/colaboradores/" + colaboradorId + "/reativar")
                .header("Authorization", "Bearer " + tokenProprietario))
            .andExpect(status().isOk());
    }

    @Test
    void reativar_socio_retorna403() throws Exception {
        mockMvc.perform(post("/api/proprietario/colaboradores/" + colaboradorId + "/reativar")
                .header("Authorization", "Bearer " + tokenSocio))
            .andExpect(status().isForbidden());
    }

    @Test
    void reativar_semToken_retorna401() throws Exception {
        mockMvc.perform(post("/api/proprietario/colaboradores/" + colaboradorId + "/reativar"))
            .andExpect(status().is4xxClientError());
    }
}
