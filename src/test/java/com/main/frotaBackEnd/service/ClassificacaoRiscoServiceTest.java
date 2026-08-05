package com.main.frotaBackEnd.service;

import com.main.frotaBackEnd.helper.TestDataFactory;
import com.main.frotaBackEnd.model.Maquina;
import com.main.frotaBackEnd.model.OrdemManutencao;
import com.main.frotaBackEnd.model.RegistroOperacao;
import com.main.frotaBackEnd.model.Usuario;
import com.main.frotaBackEnd.repository.MaquinaRepository;
import com.main.frotaBackEnd.repository.OrdemManutencaoRepository;
import com.main.frotaBackEnd.repository.RegistroOperacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClassificacaoRiscoServiceTest {

    @Mock private MaquinaRepository maquinaRepository;
    @Mock private OrdemManutencaoRepository ordemManutencaoRepository;
    @Mock private RegistroOperacaoRepository registroOperacaoRepository;

    @InjectMocks private ClassificacaoRiscoService classificacaoRiscoService;

    private Maquina maquina;
    private Usuario operador;

    @BeforeEach
    void setUp() {
        maquina = TestDataFactory.criarMaquina(1L, "Trator Test", "Disponivel", "Trator", "Baixo");
        operador = TestDataFactory.criarUsuario(10L, "Carlos", "carlos@test.com", "OPERADOR");
        when(maquinaRepository.save(any())).thenReturn(maquina);
    }

    @Test
    void recalcularRisco_comOrdemCritica_setaAlto() {
        OrdemManutencao ordem = TestDataFactory.criarOrdemManutencao(1L, maquina, operador, "Ativa");
        ordem.setPrioridade("Critica");
        when(ordemManutencaoRepository.buscarPorMaquinaId(1L)).thenReturn(List.of(ordem));
        when(registroOperacaoRepository.buscarPorMaquinaId(1L)).thenReturn(Collections.emptyList());

        classificacaoRiscoService.recalcularRisco(maquina);

        assertEquals("Alto", maquina.getNivelRisco());
    }

    @Test
    void recalcularRisco_tresOuMaisFalhas_setaAlto() {
        when(ordemManutencaoRepository.buscarPorMaquinaId(1L)).thenReturn(Collections.emptyList());

        RegistroOperacao r1 = criarRegistroComFalha("operação com falha no motor");
        RegistroOperacao r2 = criarRegistroComFalha("falha no sistema hidráulico");
        RegistroOperacao r3 = criarRegistroComFalha("falha elétrica detectada");
        when(registroOperacaoRepository.buscarPorMaquinaId(1L)).thenReturn(List.of(r1, r2, r3));

        classificacaoRiscoService.recalcularRisco(maquina);

        assertEquals("Alto", maquina.getNivelRisco());
    }

    @Test
    void recalcularRisco_umaOrdemPendente_setaMedio() {
        OrdemManutencao ordem = TestDataFactory.criarOrdemManutencao(1L, maquina, operador, "Aguardando Aprovação");
        when(ordemManutencaoRepository.buscarPorMaquinaId(1L)).thenReturn(List.of(ordem));
        when(registroOperacaoRepository.buscarPorMaquinaId(1L)).thenReturn(Collections.emptyList());

        classificacaoRiscoService.recalcularRisco(maquina);

        assertEquals("Medio", maquina.getNivelRisco());
    }

    @Test
    void recalcularRisco_umaFalha_setaMedio() {
        when(ordemManutencaoRepository.buscarPorMaquinaId(1L)).thenReturn(Collections.emptyList());
        RegistroOperacao r = criarRegistroComFalha("falha no freio");
        when(registroOperacaoRepository.buscarPorMaquinaId(1L)).thenReturn(List.of(r));

        classificacaoRiscoService.recalcularRisco(maquina);

        assertEquals("Medio", maquina.getNivelRisco());
    }

    @Test
    void recalcularRisco_semProblemas_setaBaixo() {
        when(ordemManutencaoRepository.buscarPorMaquinaId(1L)).thenReturn(Collections.emptyList());
        when(registroOperacaoRepository.buscarPorMaquinaId(1L)).thenReturn(Collections.emptyList());

        classificacaoRiscoService.recalcularRisco(maquina);

        assertEquals("Baixo", maquina.getNivelRisco());
    }

    @Test
    void determinarPrioridade_urgenciaCritica_retornaCritica() {
        String resultado = classificacaoRiscoService.determinarPrioridadeManutencao("Baixo", "Critica");
        assertEquals("Critica", resultado);
    }

    @Test
    void determinarPrioridade_riscoAlto_retornaAlta() {
        String resultado = classificacaoRiscoService.determinarPrioridadeManutencao("Alto", null);
        assertEquals("Alta", resultado);
    }

    @Test
    void determinarPrioridade_riscoMedio_retornaMedia() {
        String resultado = classificacaoRiscoService.determinarPrioridadeManutencao("Medio", null);
        assertEquals("Media", resultado);
    }

    private RegistroOperacao criarRegistroComFalha(String observacoes) {
        RegistroOperacao r = TestDataFactory.criarRegistroOperacaoEncerrado(maquina, operador);
        r.setDataFim(LocalDateTime.now().minusDays(1));
        r.setObservacoes(observacoes);
        return r;
    }
}
