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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ManutencaoServiceTest {

    @Mock private OrdemManutencaoRepository ordemManutencaoRepository;
    @Mock private MaquinaRepository maquinaRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificacaoRepository notificacaoRepository;
    @Mock private ClassificacaoRiscoService classificacaoRiscoService;

    @InjectMocks private ManutencaoService manutencaoService;

    private Maquina maquina;
    private Usuario proprietario;
    private Usuario operador;

    @BeforeEach
    void setUp() {
        maquina = TestDataFactory.criarMaquina(1L, "Trator Gamma", "Disponivel", "Trator", "Baixo");
        proprietario = TestDataFactory.criarUsuario(1L, "Dono", "dono@test.com", "PROPRIETARIO");
        operador = TestDataFactory.criarUsuario(10L, "Carlos", "carlos@test.com", "OPERADOR");
        when(classificacaoRiscoService.determinarPrioridadeManutencao(any(), any())).thenReturn("Baixa");
    }

    @Test
    void abrirOrdem_maquinaNaoEncontrada_retorna404() {
        when(maquinaRepository.findById(99L)).thenReturn(Optional.empty());
        NovaOrdemDTO dto = new NovaOrdemDTO();
        dto.setDescricao("Problema no motor");
        dto.setUrgencia("Baixa");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            manutencaoService.abrirOrdem(99L, dto, 1L, "PROPRIETARIO"));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void abrirOrdem_usuarioNaoEncontrado_retorna404() {
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        NovaOrdemDTO dto = new NovaOrdemDTO();
        dto.setDescricao("Problema");
        dto.setUrgencia("Baixa");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            manutencaoService.abrirOrdem(1L, dto, 99L, "PROPRIETARIO"));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void abrirOrdem_operadorSemVinculo_retorna403() {
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(userRepository.findById(10L)).thenReturn(Optional.of(operador));
        when(userRepository.verificaVinculo(10L, 1L)).thenReturn(false);
        NovaOrdemDTO dto = new NovaOrdemDTO();
        dto.setDescricao("Problema");
        dto.setUrgencia("Alta");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            manutencaoService.abrirOrdem(1L, dto, 10L, "OPERADOR"));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void abrirOrdem_proprietario_statusAtivo() {
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(userRepository.findById(1L)).thenReturn(Optional.of(proprietario));
        when(userRepository.findAll()).thenReturn(List.of(proprietario));
        OrdemManutencao ordemSalva = TestDataFactory.criarOrdemManutencao(10L, maquina, proprietario, "Ativa");
        when(ordemManutencaoRepository.save(any())).thenReturn(ordemSalva);
        NovaOrdemDTO dto = new NovaOrdemDTO();
        dto.setDescricao("Troca de óleo");
        dto.setUrgencia("Baixa");
        OrdemManutencaoDTO resultado = manutencaoService.abrirOrdem(1L, dto, 1L, "PROPRIETARIO");
        assertEquals("Ativa", resultado.getStatus());
    }

    @Test
    void abrirOrdem_operadorVinculado_statusAguardandoAprovacao() {
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(userRepository.findById(10L)).thenReturn(Optional.of(operador));
        when(userRepository.verificaVinculo(10L, 1L)).thenReturn(true);
        when(userRepository.findAll()).thenReturn(List.of(proprietario));
        OrdemManutencao ordemSalva = TestDataFactory.criarOrdemManutencao(10L, maquina, operador, "Aguardando Aprovação");
        when(ordemManutencaoRepository.save(any())).thenReturn(ordemSalva);
        NovaOrdemDTO dto = new NovaOrdemDTO();
        dto.setDescricao("Filtro entupido");
        dto.setUrgencia("Alta");
        OrdemManutencaoDTO resultado = manutencaoService.abrirOrdem(1L, dto, 10L, "OPERADOR");
        assertEquals("Aguardando Aprovação", resultado.getStatus());
    }

    @Test
    void aprovarOrdem_naoEncontrada_retorna404() {
        when(ordemManutencaoRepository.findById(99L)).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            manutencaoService.aprovarOrdem(99L, true));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void aprovarOrdem_statusNaoAguardando_retorna400() {
        OrdemManutencao ordem = TestDataFactory.criarOrdemManutencao(1L, maquina, operador, "Ativa");
        when(ordemManutencaoRepository.findById(1L)).thenReturn(Optional.of(ordem));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            manutencaoService.aprovarOrdem(1L, true));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void aprovarOrdem_aprovada_setaStatusAtivo() {
        OrdemManutencao ordem = TestDataFactory.criarOrdemManutencao(1L, maquina, operador, "Aguardando Aprovação");
        when(ordemManutencaoRepository.findById(1L)).thenReturn(Optional.of(ordem));
        when(ordemManutencaoRepository.save(any())).thenReturn(ordem);
        OrdemManutencaoDTO resultado = manutencaoService.aprovarOrdem(1L, true);
        assertEquals("Ativa", resultado.getStatus());
        verify(notificacaoRepository).save(any());
    }

    @Test
    void aprovarOrdem_recusada_setaStatusRecusada() {
        OrdemManutencao ordem = TestDataFactory.criarOrdemManutencao(1L, maquina, operador, "Aguardando Aprovação");
        when(ordemManutencaoRepository.findById(1L)).thenReturn(Optional.of(ordem));
        when(ordemManutencaoRepository.save(any())).thenReturn(ordem);
        OrdemManutencaoDTO resultado = manutencaoService.aprovarOrdem(1L, false);
        assertEquals("Recusada", resultado.getStatus());
    }

    @Test
    void encerrarOrdem_semObservacao_retorna400SemBuscarNoBanco() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            manutencaoService.encerrarOrdem(1L, null));
        assertEquals(400, ex.getStatusCode().value());
        verify(ordemManutencaoRepository, never()).findById(anyLong());
    }

    @Test
    void encerrarOrdem_ordemNaoAtiva_retorna400() {
        OrdemManutencao ordem = TestDataFactory.criarOrdemManutencao(1L, maquina, proprietario, "Encerrada");
        when(ordemManutencaoRepository.findById(1L)).thenReturn(Optional.of(ordem));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            manutencaoService.encerrarOrdem(1L, "Finalizou"));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void removerDaAba_ordemEncerrada_setaFlagEChaveSave() {
        OrdemManutencao ordem = TestDataFactory.criarOrdemManutencao(1L, maquina, proprietario, "Encerrada");
        when(ordemManutencaoRepository.findById(1L)).thenReturn(Optional.of(ordem));
        manutencaoService.removerDaAba(1L);
        assertTrue(ordem.isRemovidaDaAba());
        verify(ordemManutencaoRepository).save(ordem);
        verify(ordemManutencaoRepository, never()).delete(any());
    }
}
