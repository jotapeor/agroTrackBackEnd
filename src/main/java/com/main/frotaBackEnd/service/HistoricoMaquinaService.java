package com.main.frotaBackEnd.service;

import com.main.frotaBackEnd.model.*;
import com.main.frotaBackEnd.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class HistoricoMaquinaService {

    @Autowired
    private RegistroOperacaoRepository registroOperacaoRepository;

    @Autowired
    private AbastecimentoRepository abastecimentoRepository;

    @Autowired
    private OrdemManutencaoRepository ordemManutencaoRepository;

    @Autowired
    private AutorizacaoRiscoRepository autorizacaoRiscoRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    public List<HistoricoEventoDTO> obterHistoricoCompleto(Long idMaquina) {
        List<HistoricoEventoDTO> historico = new ArrayList<>();

        List<RegistroOperacao> operacoes = registroOperacaoRepository.buscarPorMaquinaId(idMaquina);
        for (RegistroOperacao op : operacoes) {
            String titulo = op.getDataFim() != null ? "Operação Encerrada" : "Operação Iniciada";
            String responsavel = op.getOperador() != null ? op.getOperador().getNome() : "Desconhecido";
            String descricao = "Hodômetro Início: " + op.getHodometroInicio();
            if (op.getHodometroFim() != null) {
                descricao += " | Hodômetro Fim: " + op.getHodometroFim();
            }
            if (op.getObservacoes() != null && !op.getObservacoes().isEmpty()) {
                descricao += " | Obs: " + op.getObservacoes();
            }
            historico.add(new HistoricoEventoDTO("REGISTRO_OPERACAO", op.getDataInicio(), titulo, descricao, responsavel, null));
        }

        List<Abastecimento> abastecimentos = abastecimentoRepository.buscarPorMaquinaId(idMaquina);
        for (Abastecimento ab : abastecimentos) {
            String descricao = ab.getLitros() + "L de " + ab.getTipoCombustivel() + ", hodômetro " + ab.getHodometroAtual() + "km";
            String responsavel = ab.getOperador() != null ? ab.getOperador().getNome() : "Desconhecido";
            historico.add(new HistoricoEventoDTO("ABASTECIMENTO", ab.getDataAbastecimento(), "Abastecimento registrado", descricao, responsavel, null));
        }

        List<OrdemManutencao> ordens = ordemManutencaoRepository.buscarPorMaquinaId(idMaquina);
        for (OrdemManutencao om : ordens) {
            String responsavel = om.getAbertaPor() != null ? om.getAbertaPor().getNome() : "Desconhecido";
            String titulo = "Ordem: " + om.getStatus();
            historico.add(new HistoricoEventoDTO("ORDEM_MANUTENCAO", om.getDataAbertura(), titulo, om.getDescricao(), responsavel, null));
        }

        List<AutorizacaoRisco> autorizacoes = autorizacaoRiscoRepository.buscarPorMaquinaId(idMaquina);
        for (AutorizacaoRisco ar : autorizacoes) {
            String responsavel = ar.getAutorizadoPor() != null ? ar.getAutorizadoPor().getNome() : "Desconhecido";
            historico.add(new HistoricoEventoDTO("AUTORIZACAO_RISCO", ar.getDataAutorizacao(), "Operação de Risco Autorizada", ar.getJustificativa(), responsavel, null));
        }

        List<Notificacao> notificacoes = notificacaoRepository.buscarPorMaquinaId(idMaquina);
        for (Notificacao notif : notificacoes) {
            String responsavel = notif.getUsuarioDestinatario() != null ? notif.getUsuarioDestinatario().getNome() : "Desconhecido";
            String titulo = "Notificação: " + notif.getTipo();
            historico.add(new HistoricoEventoDTO("NOTIFICACAO", notif.getDataCriacao(), titulo, notif.getMensagem(), responsavel, null));
        }

        historico.sort((e1, e2) -> {
            if (e1.getData() == null && e2.getData() == null) return 0;
            if (e1.getData() == null) return 1;
            if (e2.getData() == null) return -1;
            return e2.getData().compareTo(e1.getData());
        });

        return historico;
    }
}
