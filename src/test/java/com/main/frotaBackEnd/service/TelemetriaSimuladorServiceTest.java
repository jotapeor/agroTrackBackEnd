package com.main.frotaBackEnd.service;

import com.main.frotaBackEnd.helper.TestDataFactory;
import com.main.frotaBackEnd.model.Maquina;
import com.main.frotaBackEnd.model.TelemetriaMaquina;
import com.main.frotaBackEnd.repository.MaquinaRepository;
import com.main.frotaBackEnd.repository.RegistroOperacaoRepository;
import com.main.frotaBackEnd.repository.TelemetriaMaquinaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TelemetriaSimuladorServiceTest {

    @Mock private MaquinaRepository maquinaRepository;
    @Mock private TelemetriaMaquinaRepository telemetriaMaquinaRepository;
    @Mock private RegistroOperacaoRepository registroOperacaoRepository;

    @InjectMocks private TelemetriaSimuladorService telemetriaSimuladorService;

    private Maquina maquinaEmOperacao;

    @BeforeEach
    void setUp() {
        maquinaEmOperacao = TestDataFactory.criarMaquina(1L, "Trator Test", "Em Operacao", "Trator", "Baixo");

        when(telemetriaMaquinaRepository.findTopByMaquinaIdOrderByDataAtualizacaoDesc(anyLong()))
            .thenReturn(Optional.empty());
        when(registroOperacaoRepository.buscarOperacoesAtivas(anyLong()))
            .thenReturn(Collections.emptyList());
        when(telemetriaMaquinaRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
    }

    @Test
    void simularTelemetria_semMaquinas_naoSalvaTelemetria() {
        when(maquinaRepository.findAll()).thenReturn(Collections.emptyList());

        telemetriaSimuladorService.simularTelemetria();

        verify(telemetriaMaquinaRepository, never()).save(any());
    }

    @Test
    void simularTelemetria_comMaquinaEmOperacao_salvaTelemetria() {
        when(maquinaRepository.findAll()).thenReturn(List.of(maquinaEmOperacao));

        telemetriaSimuladorService.simularTelemetria();

        verify(telemetriaMaquinaRepository, times(1)).save(any(TelemetriaMaquina.class));
    }

    @Test
    void simularTelemetria_verificarCamposTelemetria() {
        when(maquinaRepository.findAll()).thenReturn(List.of(maquinaEmOperacao));
        ArgumentCaptor<TelemetriaMaquina> captor = ArgumentCaptor.forClass(TelemetriaMaquina.class);

        telemetriaSimuladorService.simularTelemetria();

        verify(telemetriaMaquinaRepository).save(captor.capture());
        TelemetriaMaquina salva = captor.getValue();
        assertNotNull(salva.getLatitude());
        assertNotNull(salva.getLongitude());
        assertNotNull(salva.getVelocidadeAtual());
        assertNotNull(salva.getConsumoAtual());
        assertNotNull(salva.getDataAtualizacao());
    }
}
