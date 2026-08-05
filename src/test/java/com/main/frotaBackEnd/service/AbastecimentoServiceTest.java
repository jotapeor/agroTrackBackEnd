package com.main.frotaBackEnd.service;

import com.main.frotaBackEnd.helper.TestDataFactory;
import com.main.frotaBackEnd.model.*;
import com.main.frotaBackEnd.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AbastecimentoServiceTest {

    @Mock private AbastecimentoRepository abastecimentoRepository;
    @Mock private MaquinaRepository maquinaRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificacaoRepository notificacaoRepository;
    @Mock private RegistroOperacaoRepository registroOperacaoRepository;

    @InjectMocks private AbastecimentoService abastecimentoService;

    private Maquina maquina;
    private Usuario proprietario;

    @BeforeEach
    void setUp() {
        maquina = TestDataFactory.criarMaquina(1L, "Trator Test", "Disponivel", "Trator", "Baixo");
        maquina.setHodometroInicial(new BigDecimal("1000"));
        proprietario = TestDataFactory.criarUsuario(99L, "Dono", "dono@test.com", "PROPRIETARIO");

        when(userRepository.findAll()).thenReturn(List.of(proprietario));
        when(notificacaoRepository.buscarPorMaquinaId(anyLong())).thenReturn(Collections.emptyList());
        when(notificacaoRepository.save(any())).thenReturn(null);
    }

    @Test
    void verificarAlertaPreventivo_hodometroProximo_geraNotificacao() {
        maquina.setIntervaloTrocaOleoHoras(500);
        BigDecimal hodometro = new BigDecimal("490");

        abastecimentoService.verificarAlertaPreventivo(maquina, hodometro);

        verify(notificacaoRepository, atLeastOnce()).save(any(Notificacao.class));
    }

    @Test
    void verificarAlertaPreventivo_hodometroDistante_naoGeraNotificacao() {
        maquina.setIntervaloTrocaOleoHoras(500);
        BigDecimal hodometro = new BigDecimal("400");

        abastecimentoService.verificarAlertaPreventivo(maquina, hodometro);

        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    void verificarAlertaPreventivo_semIntervalo_naoGeraNotificacao() {
        BigDecimal hodometro = new BigDecimal("490");

        abastecimentoService.verificarAlertaPreventivo(maquina, hodometro);

        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    void verificarAlertaPreventivo_inspecaoProxima_geraNotificacao() {
        maquina.setIntervaloInspecaoHoras(100);
        BigDecimal hodometro = new BigDecimal("90");

        abastecimentoService.verificarAlertaPreventivo(maquina, hodometro);

        verify(notificacaoRepository, atLeastOnce()).save(any(Notificacao.class));
    }

    @Test
    void registrarAbastecimento_maquinaInexistente_retorna404() {
        when(maquinaRepository.findById(99L)).thenReturn(Optional.empty());
        AbastecimentoDTO dto = new AbastecimentoDTO();
        dto.setHodometroAtual(new BigDecimal("1100"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            abastecimentoService.registrarAbastecimento(99L, dto, 10L, "PROPRIETARIO"));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void registrarAbastecimento_hodometroMenorQueUltimo_retorna400() {
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(userRepository.findById(10L)).thenReturn(Optional.of(proprietario));

        AbastecimentoDTO dto = new AbastecimentoDTO();
        dto.setHodometroAtual(new BigDecimal("500"));
        dto.setLitros(new BigDecimal("50"));
        dto.setTipoCombustivel("Diesel");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            abastecimentoService.registrarAbastecimento(1L, dto, 10L, "PROPRIETARIO"));

        assertEquals(400, ex.getStatusCode().value());
    }
}
