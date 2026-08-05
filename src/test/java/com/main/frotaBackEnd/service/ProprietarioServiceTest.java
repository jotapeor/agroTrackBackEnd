package com.main.frotaBackEnd.service;

import com.main.frotaBackEnd.helper.TestDataFactory;
import com.main.frotaBackEnd.model.Usuario;
import com.main.frotaBackEnd.repository.MaquinaRepository;
import com.main.frotaBackEnd.repository.RegistroOperacaoRepository;
import com.main.frotaBackEnd.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProprietarioServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private MaquinaRepository maquinaRepository;
    @Mock private RegistroOperacaoRepository registroOperacaoRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private ProprietarioService proprietarioService;

    private Usuario colaborador;

    @BeforeEach
    void setUp() {
        colaborador = TestDataFactory.criarUsuario(5L, "Maria", "maria@test.com", "OPERADOR");
        when(passwordEncoder.encode(anyString())).thenReturn("$hashed$");
    }

    @Test
    void registrarColaborador_emailJaCadastrado_retorna409() {
        when(userRepository.emailExiste("maria@test.com")).thenReturn(true);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            proprietarioService.registrarColaborador("Maria", "maria@test.com", "senha123", "OPERADOR", null));
        assertEquals(409, ex.getStatusCode().value());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registrarColaborador_dadosValidos_salvaColaborador() {
        when(userRepository.emailExiste("novo@test.com")).thenReturn(false);
        proprietarioService.registrarColaborador("Carlos", "novo@test.com", "senha123", "OPERADOR", null);
        verify(userRepository).save(any(Usuario.class));
    }

    @Test
    void excluirColaborador_encontrado_setaAtivoFalso() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(colaborador));
        proprietarioService.excluirColaborador(5L);
        assertFalse(colaborador.isAtivo());
        verify(userRepository).save(colaborador);
    }

    @Test
    void reativarColaborador_encontrado_setaAtivoTrue() {
        colaborador.setAtivo(false);
        when(userRepository.findById(5L)).thenReturn(Optional.of(colaborador));
        proprietarioService.reativarColaborador(5L);
        assertTrue(colaborador.isAtivo());
        verify(userRepository).save(colaborador);
    }
}
