package com.main.frotaBackEnd.service;

import com.main.frotaBackEnd.helper.TestDataFactory;
import com.main.frotaBackEnd.model.*;
import com.main.frotaBackEnd.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperacaoServiceTest {

    @Mock private MaquinaRepository maquinaRepository;
    @Mock private RegistroOperacaoRepository registroOperacaoRepository;
    @Mock private UserRepository userRepository;
    @Mock private ClassificacaoRiscoService classificacaoRiscoService;
    @Mock private AbastecimentoService abastecimentoService;
    @Mock private AbastecimentoRepository abastecimentoRepository;
    @Mock private AutorizacaoRiscoRepository autorizacaoRiscoRepository;
    @Mock private NotificacaoRepository notificacaoRepository;
    @Mock private HistoricoStatusMaquinaRepository historicoStatusMaquinaRepository;

    @InjectMocks private OperacaoService operacaoService;

    private Maquina maquina;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        maquina = TestDataFactory.criarMaquina(1L, "Trator Alpha", "Disponivel", "Trator", "Baixo");
        usuario = TestDataFactory.criarUsuario(10L, "João", "joao@test.com", "OPERADOR");
    }

    @Test
    void trocarStatus_maquinaNaoEncontrada_retorna404() {
        when(maquinaRepository.findById(99L)).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            operacaoService.trocarStatus(99L, new TrocaStatusDTO(), 10L, "OPERADOR"));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void trocarStatus_usuarioNaoEncontrado_retorna404() {
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(userRepository.findById(10L)).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            operacaoService.trocarStatus(1L, new TrocaStatusDTO(), 10L, "OPERADOR"));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void trocarStatus_operadorSemVinculo_retorna403() {
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(userRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(userRepository.verificaVinculo(10L, 1L)).thenReturn(false);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            operacaoService.trocarStatus(1L, TestDataFactory.criarTrocaStatusDTO("Em Operacao", null, null, null), 10L, "OPERADOR"));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void trocarStatus_proprietarioPulaVerificacaoVinculo() {
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(userRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(registroOperacaoRepository.buscarOperacoesAtivas(1L)).thenReturn(List.of());
        operacaoService.trocarStatus(1L, TestDataFactory.criarTrocaStatusDTOIniciar(true, null), 10L, "PROPRIETARIO");
        verify(userRepository, never()).verificaVinculo(anyLong(), anyLong());
        verify(registroOperacaoRepository).save(any());
    }

    @Test
    void trocarStatus_mesmoStatus_retorna400() {
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(userRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(userRepository.verificaVinculo(10L, 1L)).thenReturn(true);
        when(registroOperacaoRepository.buscarOperacoesAtivas(1L)).thenReturn(List.of());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            operacaoService.trocarStatus(1L, TestDataFactory.criarTrocaStatusDTO("Disponivel", null, null, null), 10L, "OPERADOR"));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void trocarStatus_semConfirmacao_retorna400() {
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(userRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(userRepository.verificaVinculo(10L, 1L)).thenReturn(true);
        when(registroOperacaoRepository.buscarOperacoesAtivas(1L)).thenReturn(List.of());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            operacaoService.trocarStatus(1L, TestDataFactory.criarTrocaStatusDTOIniciar(false, null), 10L, "OPERADOR"));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void trocarStatus_pulverizadorSemPeso_retorna400() {
        maquina.setTipo("Pulverizador");
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(userRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(userRepository.verificaVinculo(10L, 1L)).thenReturn(true);
        when(registroOperacaoRepository.buscarOperacoesAtivas(1L)).thenReturn(List.of());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            operacaoService.trocarStatus(1L, TestDataFactory.criarTrocaStatusDTOIniciar(true, null), 10L, "OPERADOR"));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void trocarStatus_operadorNaoPodeInativar_retorna403() {
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(userRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(userRepository.verificaVinculo(10L, 1L)).thenReturn(true);
        when(registroOperacaoRepository.buscarOperacoesAtivas(1L)).thenReturn(List.of());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            operacaoService.trocarStatus(1L, TestDataFactory.criarTrocaStatusDTO("Inativa", null, null, null), 10L, "OPERADOR"));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void trocarStatus_operadorNaoPodeEnviarParaManutencao_retorna403() {
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(userRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(userRepository.verificaVinculo(10L, 1L)).thenReturn(true);
        when(registroOperacaoRepository.buscarOperacoesAtivas(1L)).thenReturn(List.of());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            operacaoService.trocarStatus(1L, TestDataFactory.criarTrocaStatusDTO("Em Manutencao", null, null, null), 10L, "OPERADOR"));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void trocarStatus_emManutencaoParaEmOperacao_retorna400() {
        maquina.setStatus("Em Manutencao");
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(userRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(userRepository.verificaVinculo(10L, 1L)).thenReturn(true);
        when(registroOperacaoRepository.buscarOperacoesAtivas(1L)).thenReturn(List.of());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            operacaoService.trocarStatus(1L, TestDataFactory.criarTrocaStatusDTOIniciar(true, null), 10L, "OPERADOR"));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void trocarStatus_riscoAltoSemAutorizacao_retorna403() {
        maquina.setNivelRisco("Alto");
        maquina.setAutorizadaOperacaoRisco(false);
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(userRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(userRepository.verificaVinculo(10L, 1L)).thenReturn(true);
        when(registroOperacaoRepository.buscarOperacoesAtivas(1L)).thenReturn(List.of());
        when(userRepository.findAll()).thenReturn(List.of());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            operacaoService.trocarStatus(1L, TestDataFactory.criarTrocaStatusDTOIniciar(true, null), 10L, "OPERADOR"));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void trocarStatus_riscoAltoComAutorizacao_criaRegistroELimpaFlag() {
        maquina.setNivelRisco("Alto");
        maquina.setAutorizadaOperacaoRisco(true);
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(userRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(userRepository.verificaVinculo(10L, 1L)).thenReturn(true);
        when(registroOperacaoRepository.buscarOperacoesAtivas(1L)).thenReturn(List.of());
        operacaoService.trocarStatus(1L, TestDataFactory.criarTrocaStatusDTOIniciar(true, null), 10L, "OPERADOR");
        assertFalse(maquina.isAutorizadaOperacaoRisco());
        verify(registroOperacaoRepository).save(any());
    }

    @Test
    void trocarStatus_encerrarSemHodometro_retorna400() {
        maquina.setStatus("Em Operacao");
        RegistroOperacao registro = TestDataFactory.criarRegistroOperacaoAtivo(maquina, usuario);
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(userRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(userRepository.verificaVinculo(10L, 1L)).thenReturn(true);
        when(registroOperacaoRepository.buscarOperacoesAtivas(1L)).thenReturn(List.of(registro));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            operacaoService.trocarStatus(1L, TestDataFactory.criarTrocaStatusDTO("Disponivel", null, null, null), 10L, "OPERADOR"));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void trocarStatus_hodometroFimMenorQueInicio_retorna400() {
        maquina.setStatus("Em Operacao");
        RegistroOperacao registro = TestDataFactory.criarRegistroOperacaoAtivo(maquina, usuario);
        registro.setHodometroInicio(new BigDecimal("1000"));
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(userRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(userRepository.verificaVinculo(10L, 1L)).thenReturn(true);
        when(registroOperacaoRepository.buscarOperacoesAtivas(1L)).thenReturn(List.of(registro));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            operacaoService.trocarStatus(1L, TestDataFactory.criarTrocaStatusDTO("Disponivel", new BigDecimal("500"), null, null), 10L, "OPERADOR"));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void trocarStatus_encerrarComSucesso_salvaRegistroESetaDataFim() {
        maquina.setStatus("Em Operacao");
        RegistroOperacao registro = TestDataFactory.criarRegistroOperacaoAtivo(maquina, usuario);
        registro.setHodometroInicio(new BigDecimal("1000"));
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(userRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(userRepository.verificaVinculo(10L, 1L)).thenReturn(true);
        when(registroOperacaoRepository.buscarOperacoesAtivas(1L)).thenReturn(List.of(registro));
        when(abastecimentoRepository.buscarPorMaquinaEIntervalo(any(), any(), any())).thenReturn(List.of());
        when(notificacaoRepository.buscarPorMaquinaEIntervalo(any(), any(), any())).thenReturn(List.of());
        operacaoService.trocarStatus(1L, TestDataFactory.criarTrocaStatusDTO("Disponivel", new BigDecimal("1200"), "Observação final", null), 10L, "OPERADOR");
        assertNotNull(registro.getDataFim());
        verify(registroOperacaoRepository, atLeast(1)).save(any());
    }

    @Test
    void trocarStatus_inativarSemMotivo_retorna400() {
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(userRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(registroOperacaoRepository.buscarOperacoesAtivas(1L)).thenReturn(List.of());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            operacaoService.trocarStatus(1L, TestDataFactory.criarTrocaStatusDTO("Inativa", null, null, null), 10L, "PROPRIETARIO"));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void trocarStatus_inativarComMotivo_criaHistorico() {
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(userRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(registroOperacaoRepository.buscarOperacoesAtivas(1L)).thenReturn(List.of());
        operacaoService.trocarStatus(1L, TestDataFactory.criarTrocaStatusDTO("Inativa", null, null, "Quebrou"), 10L, "PROPRIETARIO");
        verify(historicoStatusMaquinaRepository).save(any());
    }

    @Test
    void trocarStatus_socioInativa_notificaProprietarios() {
        usuario.setPerfil("SOCIO");
        Usuario proprietario = TestDataFactory.criarUsuario(1L, "Prop", "prop@test.com", "PROPRIETARIO");
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(userRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(userRepository.verificaVinculo(10L, 1L)).thenReturn(true);
        when(registroOperacaoRepository.buscarOperacoesAtivas(1L)).thenReturn(List.of());
        when(userRepository.findAll()).thenReturn(List.of(proprietario));
        operacaoService.trocarStatus(1L, TestDataFactory.criarTrocaStatusDTO("Inativa", null, null, "Revisão"), 10L, "SOCIO");
        verify(notificacaoRepository, atLeast(1)).save(any());
    }

    @Test
    void trocarStatus_outroOperadorTentaEncerrar_retorna403() {
        maquina.setStatus("Em Operacao");
        Usuario outroOperador = TestDataFactory.criarUsuario(20L, "Outro", "outro@test.com", "OPERADOR");
        RegistroOperacao registro = TestDataFactory.criarRegistroOperacaoAtivo(maquina, outroOperador);
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(userRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(userRepository.verificaVinculo(10L, 1L)).thenReturn(true);
        when(registroOperacaoRepository.buscarOperacoesAtivas(1L)).thenReturn(List.of(registro));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            operacaoService.trocarStatus(1L, TestDataFactory.criarTrocaStatusDTO("Disponivel", new BigDecimal("1100"), null, null), 10L, "OPERADOR"));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void obterOperacaoAtiva_semOperacaoAtiva_retornaNull() {
        when(registroOperacaoRepository.buscarOperacoesAtivas(1L)).thenReturn(List.of());
        assertNull(operacaoService.obterOperacaoAtiva(1L));
    }

    @Test
    void obterOperacaoAtiva_comOperacaoAtiva_retornaDTO() {
        RegistroOperacao registro = TestDataFactory.criarRegistroOperacaoAtivo(maquina, usuario);
        when(registroOperacaoRepository.buscarOperacoesAtivas(1L)).thenReturn(List.of(registro));
        when(abastecimentoRepository.buscarPorMaquinaEIntervalo(any(), any(), any())).thenReturn(List.of());
        assertNotNull(operacaoService.obterOperacaoAtiva(1L));
    }

    @Test
    void trocarStatus_semeadeiraSemPeso_retorna400() {
        maquina.setTipo("Semeadeira");
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(userRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(userRepository.verificaVinculo(10L, 1L)).thenReturn(true);
        when(registroOperacaoRepository.buscarOperacoesAtivas(1L)).thenReturn(List.of());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            operacaoService.trocarStatus(1L, TestDataFactory.criarTrocaStatusDTOIniciar(true, null), 10L, "OPERADOR"));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void listarHistoricoMaquina_retornaListaVaziaQuandoSemDados() {
        when(registroOperacaoRepository.buscarPorMaquinaId(1L)).thenReturn(List.of());
        when(abastecimentoRepository.buscarPorMaquinaId(1L)).thenReturn(List.of());
        when(autorizacaoRiscoRepository.buscarPorMaquinaId(1L)).thenReturn(List.of());
        when(historicoStatusMaquinaRepository.buscarPorMaquinaId(1L)).thenReturn(List.of());
        List<Map<String, Object>> resultado = operacaoService.listarHistoricoMaquina(1L);
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void trocarStatus_operadorVinculadoComConfirmacao_criaRegistroOperacao() {
        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(userRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(userRepository.verificaVinculo(10L, 1L)).thenReturn(true);
        when(registroOperacaoRepository.buscarOperacoesAtivas(1L)).thenReturn(List.of());
        operacaoService.trocarStatus(1L, TestDataFactory.criarTrocaStatusDTOIniciar(true, null), 10L, "OPERADOR");
        verify(registroOperacaoRepository).save(any());
        assertEquals("Em Operacao", maquina.getStatus());
    }
}
