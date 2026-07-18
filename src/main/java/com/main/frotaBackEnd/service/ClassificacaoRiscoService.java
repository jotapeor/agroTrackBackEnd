package com.main.frotaBackEnd.service;

import com.main.frotaBackEnd.model.Maquina;
import com.main.frotaBackEnd.model.OrdemManutencao;
import com.main.frotaBackEnd.model.RegistroOperacao;
import com.main.frotaBackEnd.repository.MaquinaRepository;
import com.main.frotaBackEnd.repository.OrdemManutencaoRepository;
import com.main.frotaBackEnd.repository.RegistroOperacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClassificacaoRiscoService {

    @Autowired
    private MaquinaRepository maquinaRepository;

    @Autowired
    private OrdemManutencaoRepository ordemManutencaoRepository;

    @Autowired
    private RegistroOperacaoRepository registroOperacaoRepository;

    public void recalcularRisco(Maquina maquina) {
        List<OrdemManutencao> manutencoesPendentes = ordemManutencaoRepository.buscarPorMaquinaId(maquina.getId()).stream()
                .filter(o -> "Aguardando Aprovação".equals(o.getStatus()) || "Ativa".equals(o.getStatus()))
                .toList();

        long qtdManutencoes = manutencoesPendentes.size();
        boolean temCritica = manutencoesPendentes.stream().anyMatch(o -> "Critica".equalsIgnoreCase(o.getPrioridade()));

        LocalDateTime trintaDiasAtras = LocalDateTime.now().minusDays(30);
        List<RegistroOperacao> operacoesRecentes = registroOperacaoRepository.buscarPorMaquinaId(maquina.getId()).stream()
                .filter(r -> r.getDataFim() != null && r.getDataFim().isAfter(trintaDiasAtras))
                .toList();

        long falhas = operacoesRecentes.stream()
                .filter(r -> r.getObservacoes() != null && r.getObservacoes().toLowerCase().contains("falha"))
                .count();

        String novoRisco = "Baixo";

        if (temCritica || falhas >= 3) {
            novoRisco = "Alto";
        } else if (qtdManutencoes >= 1 || falhas >= 1) {
            novoRisco = "Medio";
        }

        maquina.setNivelRisco(novoRisco);
        maquinaRepository.save(maquina);
    }

    public String determinarPrioridadeManutencao(String nivelRisco, String urgenciaFalha) {
        if ("Critica".equalsIgnoreCase(urgenciaFalha)) {
            return "Critica";
        }
        if ("Alto".equalsIgnoreCase(nivelRisco)) {
            return "Alta";
        } else if ("Medio".equalsIgnoreCase(nivelRisco)) {
            return "Media";
        }
        return "Baixa";
    }
}
