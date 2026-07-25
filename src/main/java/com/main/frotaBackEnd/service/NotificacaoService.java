package com.main.frotaBackEnd.service;

import com.main.frotaBackEnd.model.Notificacao;
import com.main.frotaBackEnd.model.NotificacaoDTO;
import com.main.frotaBackEnd.repository.NotificacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificacaoService {

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    public List<NotificacaoDTO> listarPorUsuario(Long idUsuario) {
        return notificacaoRepository.buscarPorUsuarioId(idUsuario).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void marcarComoLida(Long idNotificacao, Long idUsuarioLogado) {
        Notificacao notificacao = notificacaoRepository.findById(idNotificacao)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Notificação não encontrada."));

        if (!notificacao.getUsuarioDestinatario().getId_usuario().equals(idUsuarioLogado)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Você não pode alterar uma notificação de outro usuário.");
        }

        notificacao.setLida(true);
        notificacaoRepository.save(notificacao);
    }

    @Transactional
    public void remover(Long idNotificacao, Long idUsuarioLogado) {
        Notificacao notificacao = notificacaoRepository.findById(idNotificacao)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Notificação não encontrada."));

        if (!notificacao.getUsuarioDestinatario().getId_usuario().equals(idUsuarioLogado)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Você não pode remover uma notificação de outro usuário.");
        }

        notificacaoRepository.delete(notificacao);
    }

    private NotificacaoDTO toDTO(Notificacao n) {
        NotificacaoDTO dto = new NotificacaoDTO();
        dto.setId(n.getId());
        dto.setTipo(n.getTipo());
        dto.setMensagem(n.getMensagem());
        dto.setLida(n.isLida());
        dto.setDataCriacao(n.getDataCriacao());
        if (n.getOrdemManutencao() != null) {
            dto.setIdOrdem(n.getOrdemManutencao().getId());
            dto.setDescricaoOrdem(n.getOrdemManutencao().getDescricao());
            dto.setPrioridadeOrdem(n.getOrdemManutencao().getPrioridade());
            dto.setStatusOrdem(n.getOrdemManutencao().getStatus());
        }
        return dto;
    }
}
