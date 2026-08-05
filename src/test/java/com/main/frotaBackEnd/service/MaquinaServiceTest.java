package com.main.frotaBackEnd.service;

import com.main.frotaBackEnd.helper.TestDataFactory;
import com.main.frotaBackEnd.model.Maquina;
import com.main.frotaBackEnd.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaquinaServiceTest {

    @Mock private MaquinaRepository maquinaRepository;
    @Mock private FazendaRepository fazendaRepository;
    @Mock private TalhaoRepository talhaoRepository;
    @Mock private AutorizacaoRiscoRepository autorizacaoRiscoRepository;
    @Mock private UserRepository userRepository;
    @Mock private MaquinaCombustivelRepository maquinaCombustivelRepository;
    @Mock private RegistroOperacaoRepository registroOperacaoRepository;
    @Mock private EntityManager entityManager;

    @InjectMocks private MaquinaService maquinaService;

    private Maquina maquina;

    @BeforeEach
    void setUp() {
        maquina = TestDataFactory.criarMaquina(1L, "Trator Beta", "Disponivel", "Trator", "Baixo");
    }

    @Test
    void buscarPorId_encontrada_retornaDTO() {
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        assertNotNull(maquinaService.buscarPorId(1L));
    }

    @Test
    void buscarPorId_naoEncontrada_retorna404() {
        when(maquinaRepository.findById(99L)).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            maquinaService.buscarPorId(99L));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void excluir_maquinaEmOperacao_retorna400() {
        maquina.setStatus("Em Operacao");
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            maquinaService.excluir(1L));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void excluir_maquinaDisponivel_setaAtivoFalso() {
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(maquinaRepository.save(any())).thenReturn(maquina);
        maquinaService.excluir(1L);
        assertFalse(maquina.isAtivo());
        verify(maquinaRepository).save(maquina);
    }

    @Test
    void reativar_maquinaArquivada_setaAtivoTrueEStatusDisponivel() {
        maquina.setAtivo(false);
        maquina.setStatus("Inativa");
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(maquinaRepository.save(any())).thenReturn(maquina);
        maquinaService.reativar(1L);
        assertTrue(maquina.isAtivo());
        assertEquals("Disponivel", maquina.getStatus());
    }
}
