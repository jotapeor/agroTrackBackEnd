package com.main.frotaBackEnd;

import com.main.frotaBackEnd.model.*;
import com.main.frotaBackEnd.repository.AbastecimentoRepository;
import com.main.frotaBackEnd.repository.MaquinaRepository;
import com.main.frotaBackEnd.repository.NotificacaoRepository;
import com.main.frotaBackEnd.repository.RegistroOperacaoRepository;
import com.main.frotaBackEnd.repository.UserRepository;
import com.main.frotaBackEnd.service.AbastecimentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class AbastecimentoConsumoTest {

    @InjectMocks
    private AbastecimentoService abastecimentoService;

    @Mock
    private AbastecimentoRepository abastecimentoRepository;
    @Mock
    private MaquinaRepository maquinaRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificacaoRepository notificacaoRepository;
    @Mock
    private RegistroOperacaoRepository registroOperacaoRepository;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAnomaliaSemPesoGeraNotificacao() {
        Maquina maquina = new Maquina();
        maquina.setId(1L);
        maquina.setNome("Trator 1");
        maquina.setHodometroInicial(new BigDecimal("100"));
        maquina.setConsumoMedio(new BigDecimal("10.0"));
        maquina.setCapacidadeTanque(new BigDecimal("1000.0"));

        Usuario user = new Usuario();
        user.setId_usuario(1L);
        user.setPerfil("PROPRIETARIO");

        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Usuario prop = new Usuario();
        prop.setId_usuario(1L);
        prop.setPerfil("PROPRIETARIO");
        when(userRepository.findAll()).thenReturn(Arrays.asList(prop));

        Abastecimento ant = new Abastecimento();
        ant.setHodometroAtual(new BigDecimal("100"));
        ant.setDataAbastecimento(LocalDateTime.now().minusDays(1));

        Abastecimento atualSalvo = new Abastecimento();
        atualSalvo.setHodometroAtual(new BigDecimal("200"));
        atualSalvo.setLitros(new BigDecimal("1400.0"));
        atualSalvo.setDataAbastecimento(LocalDateTime.now());

        when(abastecimentoRepository.save(any(Abastecimento.class))).thenReturn(atualSalvo);

        when(abastecimentoRepository.buscarPorMaquinaId(1L)).thenReturn(Arrays.asList(atualSalvo, ant));

        when(registroOperacaoRepository.buscarPorMaquinaIdEIntervalo(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList());

        AbastecimentoDTO dto = new AbastecimentoDTO();
        dto.setHodometroAtual(new BigDecimal("200"));
        dto.setLitros(new BigDecimal("1400.0"));
        dto.setTipoCombustivel("Diesel");

        abastecimentoService.registrarAbastecimento(1L, dto, 1L, "PROPRIETARIO");

        verify(notificacaoRepository, times(1)).save(any(Notificacao.class));
    }

    @Test
    public void testAnomaliaComPesoAltoNaoGeraNotificacao() {
        Maquina maquina = new Maquina();
        maquina.setId(1L);
        maquina.setNome("Trator 1");
        maquina.setHodometroInicial(new BigDecimal("100"));
        maquina.setConsumoMedio(new BigDecimal("10.0"));
        maquina.setCapacidadeTanque(new BigDecimal("1000.0"));

        Usuario user = new Usuario();
        user.setId_usuario(1L);
        user.setPerfil("PROPRIETARIO");

        when(maquinaRepository.findById(1L)).thenReturn(Optional.of(maquina));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Usuario prop = new Usuario();
        prop.setId_usuario(1L);
        prop.setPerfil("PROPRIETARIO");
        when(userRepository.findAll()).thenReturn(Arrays.asList(prop));

        Abastecimento ant = new Abastecimento();
        ant.setHodometroAtual(new BigDecimal("100"));
        ant.setDataAbastecimento(LocalDateTime.now().minusDays(1));

        Abastecimento atualSalvo = new Abastecimento();
        atualSalvo.setHodometroAtual(new BigDecimal("200"));
        atualSalvo.setLitros(new BigDecimal("1400.0"));
        atualSalvo.setDataAbastecimento(LocalDateTime.now());

        when(abastecimentoRepository.save(any(Abastecimento.class))).thenReturn(atualSalvo);

        when(abastecimentoRepository.buscarPorMaquinaId(1L)).thenReturn(Arrays.asList(atualSalvo, ant));

        RegistroOperacao op = new RegistroOperacao();
        op.setPesoCarregado(new BigDecimal("1000.0"));
        when(registroOperacaoRepository.buscarPorMaquinaIdEIntervalo(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(op));

        AbastecimentoDTO dto = new AbastecimentoDTO();
        dto.setHodometroAtual(new BigDecimal("200"));
        dto.setLitros(new BigDecimal("1400.0"));
        dto.setTipoCombustivel("Diesel");

        abastecimentoService.registrarAbastecimento(1L, dto, 1L, "PROPRIETARIO");

        verify(notificacaoRepository, never()).save(any(Notificacao.class));
    }
}
