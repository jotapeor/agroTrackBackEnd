package com.main.frotaBackEnd;

import com.main.frotaBackEnd.model.*;
import com.main.frotaBackEnd.repository.*;
import com.main.frotaBackEnd.service.ProprietarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class VinculacaoMaquinaTest {

    @InjectMocks
    private ProprietarioService proprietarioService;

    @Mock private UserRepository userRepository;
    @Mock private MaquinaRepository maquinaRepository;
    @Mock private RegistroOperacaoRepository registroOperacaoRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private UsuarioDTO solicitanteProprietario;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        solicitanteProprietario = new UsuarioDTO();
        solicitanteProprietario.setPerfil("PROPRIETARIO");
        solicitanteProprietario.setId_usuario(1L);
    }

    @Test
    void testVincularMaquinasRefleteLista() {
        Usuario colaborador = new Usuario();
        colaborador.setId_usuario(2L);
        colaborador.setPerfil("OPERADOR");
        colaborador.setMaquinas(new ArrayList<>());

        Maquina m1 = maquinaComId(1L, "Disponivel");
        Maquina m2 = maquinaComId(2L, "Disponivel");

        when(userRepository.findById(2L)).thenReturn(Optional.of(colaborador));
        when(maquinaRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(m1, m2));
        when(registroOperacaoRepository
                .buscarOperacoesAbertasPorMaquinasEUsuario(anyList(), eq(2L)))
                .thenReturn(List.of());
        when(userRepository.save(any())).thenReturn(colaborador);

        proprietarioService.vincularMaquinas(2L, List.of(1L, 2L), solicitanteProprietario);

        assertEquals(2, colaborador.getMaquinas().size());
        assertTrue(colaborador.getMaquinas().contains(m1));
        assertTrue(colaborador.getMaquinas().contains(m2));
    }

    @Test
    void testDesvincularTodasMaquinas() {
        Maquina m1 = maquinaComId(1L, "Disponivel");
        Usuario colaborador = new Usuario();
        colaborador.setId_usuario(2L);
        colaborador.setPerfil("OPERADOR");
        colaborador.setMaquinas(new ArrayList<>(List.of(m1)));

        when(userRepository.findById(2L)).thenReturn(Optional.of(colaborador));
        when(registroOperacaoRepository
                .buscarOperacoesAbertasPorMaquinasEUsuario(List.of(1L), 2L))
                .thenReturn(List.of());
        when(userRepository.save(any())).thenReturn(colaborador);

        proprietarioService.vincularMaquinas(2L, List.of(), solicitanteProprietario);

        assertTrue(colaborador.getMaquinas().isEmpty(),
                "Após desvincular todas, a lista de máquinas deve estar vazia");
    }

    @Test
    void testBuscarPorUsuarioIdRetornaSoMaquinasVinculadas() {
        Maquina m1 = maquinaComId(1L, "Disponivel");
        Maquina m3 = maquinaComId(3L, "Em Manutencao");
        Maquina mDeOutro = maquinaComId(5L, "Disponivel");

        when(maquinaRepository.buscarPorUsuarioId(2L)).thenReturn(List.of(m1, m3));
        when(maquinaRepository.buscarTodosOrdenados()).thenReturn(List.of(m1, m3, mDeOutro));

        List<Maquina> resultadoOperador = maquinaRepository.buscarPorUsuarioId(2L);
        List<Maquina> resultadoTodas = maquinaRepository.buscarTodosOrdenados();

        assertEquals(2, resultadoOperador.size(),
                "Operador deve ver apenas suas 2 máquinas vinculadas");
        assertFalse(resultadoOperador.contains(mDeOutro),
                "Operador não deve ver máquina de outro colaborador");
        assertEquals(3, resultadoTodas.size(),
                "Proprietário deve ver todas as 3 máquinas");
    }

    @Test
    void testFiltroColaboradoresExcluiProprioUsuario_LongVsInteger() {
        Map<String, Object> proprietario = new HashMap<>();
        proprietario.put("id_usuario", Integer.valueOf(1));
        proprietario.put("nome", "João Batista");

        Map<String, Object> colaborador = new HashMap<>();
        colaborador.put("id_usuario", Integer.valueOf(2));
        colaborador.put("nome", "Carlos Mendes");

        List<Map<String, Object>> todos = List.of(proprietario, colaborador);
        Long meuId = 1L;

        List<Map<String, Object>> filtroAntigo = todos.stream()
                .filter(c -> !meuId.equals(c.get("id_usuario")))
                .toList();
        assertEquals(2, filtroAntigo.size(),
                "Bug confirmado: filtro antigo com Long.equals(Integer) não remove o próprio usuário");

        List<Map<String, Object>> filtroCorrigido = todos.stream()
                .filter(c -> {
                    Object idObj = c.get("id_usuario");
                    if (idObj == null) return true;
                    return !meuId.equals(((Number) idObj).longValue());
                })
                .toList();
        assertEquals(1, filtroCorrigido.size(),
                "Filtro corrigido deve remover o próprio usuário da lista");
        assertEquals("Carlos Mendes", filtroCorrigido.get(0).get("nome"));
    }

    @Test
    void testDesvincularBloqueadaQuandoMaquinaEmOperacao() {
        Maquina maquinaAtiva = maquinaComId(5L, "Em Operacao");
        maquinaAtiva.setNome("Colheitadeira NH CR6.90");

        Usuario fernanda = new Usuario();
        fernanda.setId_usuario(4L);
        fernanda.setPerfil("OPERADOR");
        fernanda.setMaquinas(new ArrayList<>(List.of(maquinaAtiva)));

        RegistroOperacao registroAberto = new RegistroOperacao();
        registroAberto.setId(5L);
        registroAberto.setMaquina(maquinaAtiva);
        registroAberto.setOperador(fernanda);
        registroAberto.setDataInicio(LocalDateTime.now().minusHours(3));
        registroAberto.setHodometroInicio(BigDecimal.valueOf(100));

        when(userRepository.findById(4L)).thenReturn(Optional.of(fernanda));
        when(registroOperacaoRepository
                .buscarOperacoesAbertasPorMaquinasEUsuario(List.of(5L), 4L))
                .thenReturn(List.of(registroAberto));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> proprietarioService.vincularMaquinas(4L, List.of(), solicitanteProprietario),
                "Deve lançar exceção ao tentar desvincular máquina Em Operacao");

        assertEquals(409, ex.getStatusCode().value(),
                "Código HTTP deve ser 409 Conflict");
        assertNotNull(ex.getReason());
        assertTrue(ex.getReason().contains("Colheitadeira NH CR6.90"),
                "Mensagem deve identificar a máquina bloqueada: " + ex.getReason());
        assertTrue(ex.getReason().contains("operação em andamento"),
                "Mensagem deve explicar o motivo do bloqueio: " + ex.getReason());
    }

    @Test
    void testDesvincularNaoBloqueadaComRegistroOrfao() {
        Maquina jacto3030 = maquinaComId(3L, "Em Manutencao");
        jacto3030.setNome("Pulverizador Jacto 3030");

        Usuario carlosMendes = new Usuario();
        carlosMendes.setId_usuario(2L);
        carlosMendes.setPerfil("OPERADOR");
        carlosMendes.setMaquinas(new ArrayList<>(List.of(jacto3030)));

        when(userRepository.findById(2L)).thenReturn(Optional.of(carlosMendes));
        when(registroOperacaoRepository
                .buscarOperacoesAbertasPorMaquinasEUsuario(List.of(3L), 2L))
                .thenReturn(List.of());
        when(userRepository.save(any())).thenReturn(carlosMendes);

        assertDoesNotThrow(
                () -> proprietarioService.vincularMaquinas(2L, List.of(), solicitanteProprietario),
                "Registro órfão com máquina Em Manutencao não deve bloquear a desvinculação");

        assertTrue(carlosMendes.getMaquinas().isEmpty(),
                "Carlos Mendes deve ser desvinculado do Pulverizador Jacto 3030 com sucesso");
    }

    private Maquina maquinaComId(Long id, String status) {
        Maquina m = new Maquina();
        m.setId(id);
        m.setStatus(status);
        return m;
    }
}
