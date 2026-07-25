package com.main.frotaBackEnd.service;

import com.main.frotaBackEnd.model.DashboardDTO;
import com.main.frotaBackEnd.model.Maquina;
import com.main.frotaBackEnd.model.Notificacao;
import com.main.frotaBackEnd.model.RegistroOperacao;
import com.main.frotaBackEnd.model.Usuario;
import com.main.frotaBackEnd.repository.MaquinaRepository;
import com.main.frotaBackEnd.repository.NotificacaoRepository;
import com.main.frotaBackEnd.repository.RegistroOperacaoRepository;
import com.main.frotaBackEnd.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private MaquinaRepository maquinaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private RegistroOperacaoRepository registroOperacaoRepository;

    public DashboardDTO obterDadosDashboard(Long idUsuarioLogado, String perfilUsuario) {
        List<Maquina> maquinas;
        if ("PROPRIETARIO".equals(perfilUsuario) || "SOCIO".equals(perfilUsuario)) {
            maquinas = maquinaRepository.findAll();
        } else {
            Usuario usuario = userRepository.findById(idUsuarioLogado).orElse(null);
            if (usuario != null) {
                maquinas = usuario.getMaquinas();
            } else {
                maquinas = List.of();
            }
        }

        Map<String, Long> statusCount = new HashMap<>();
        statusCount.put("Disponivel", 0L);
        statusCount.put("Em Operacao", 0L);
        statusCount.put("Em Manutencao", 0L);

        long maquinasRiscoAlto = 0;

        for (Maquina m : maquinas) {
            if (!m.isAtivo()) continue;

            String st = m.getStatus();
            if (st != null && statusCount.containsKey(st)) {
                statusCount.put(st, statusCount.get(st) + 1);
            }
            if ("Alto".equals(m.getNivelRisco())) {
                maquinasRiscoAlto++;
            }
        }

        List<Notificacao> notificacoes = notificacaoRepository.buscarPorUsuarioId(idUsuarioLogado);

        long alertasAtivos = notificacoes.stream()
                .filter(n -> !n.isLida() && (n.getTipo().startsWith("alerta") || n.getTipo().startsWith("anomalia")))
                .count();

        DashboardDTO dto = new DashboardDTO();
        dto.setMaquinasPorStatus(statusCount);
        dto.setAlertasAtivos(alertasAtivos);
        dto.setMaquinasRiscoAlto(maquinasRiscoAlto);

        dto.setNotificacoesRecentes(notificacaoService.listarPorUsuario(idUsuarioLogado).stream().limit(5).collect(Collectors.toList()));

        List<RegistroOperacao> opAberta = registroOperacaoRepository.buscarOperacaoAbertaPorUsuario(idUsuarioLogado);
        if (!opAberta.isEmpty()) {
            RegistroOperacao op = opAberta.get(0);
            Map<String, Object> opMap = new HashMap<>();
            opMap.put("idMaquina", op.getMaquina().getId());
            opMap.put("nomeMaquina", op.getMaquina().getNome());
            opMap.put("dataInicio", op.getDataInicio().toString());
            dto.setOperacaoAtiva(opMap);
        }

        return dto;
    }
}
