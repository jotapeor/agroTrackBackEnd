package com.main.frotaBackEnd.service;

import com.main.frotaBackEnd.model.*;
import com.main.frotaBackEnd.repository.MaquinaRepository;
import com.main.frotaBackEnd.repository.NotificacaoRepository;
import com.main.frotaBackEnd.repository.OrdemManutencaoRepository;
import com.main.frotaBackEnd.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ManutencaoService {

    @Autowired
    private OrdemManutencaoRepository ordemManutencaoRepository;

    @Autowired
    private MaquinaRepository maquinaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private ClassificacaoRiscoService classificacaoRiscoService;

    @Transactional
    public OrdemManutencaoDTO abrirOrdem(Long idMaquina, NovaOrdemDTO dto, Long idUsuarioLogado, String perfilUsuario) {
        Maquina maquina = maquinaRepository.findById(idMaquina)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Máquina não encontrada."));

        Usuario usuario = userRepository.findById(idUsuarioLogado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Usuário não encontrado."));

        if (!"PROPRIETARIO".equals(perfilUsuario) && !"SOCIO".equals(perfilUsuario)) {
            boolean vinculado = userRepository.verificaVinculo(idUsuarioLogado, idMaquina);
            if (!vinculado) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Você não tem permissão para esta máquina.");
            }
        }

        String prioridade = classificacaoRiscoService.determinarPrioridadeManutencao(maquina.getNivelRisco(), dto.getUrgencia());
        String status = ("PROPRIETARIO".equals(perfilUsuario) || "SOCIO".equals(perfilUsuario)) ? "Ativa" : "Aguardando Aprovação";

        OrdemManutencao ordem = new OrdemManutencao();
        ordem.setMaquina(maquina);
        ordem.setAbertaPor(usuario);
        ordem.setStatus(status);
        ordem.setPrioridade(prioridade);
        ordem.setDescricao(dto.getDescricao());
        ordem.setDataAbertura(LocalDateTime.now());

        ordem = ordemManutencaoRepository.save(ordem);

        if ("Aguardando Aprovação".equals(status)) {
            gerarNotificacaoProprietario("ordem_pendente", "Nova ordem de manutenção aberta por " + usuario.getNome() + " para a máquina " + maquina.getNome() + " aguardando aprovação.");
        }

        return toDTO(ordem);
    }

    @Transactional
    public OrdemManutencaoDTO aprovarOrdem(Long idOrdem, boolean aprovada) {
        OrdemManutencao ordem = ordemManutencaoRepository.findById(idOrdem)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Ordem não encontrada."));

        if (!"Aguardando Aprovação".equals(ordem.getStatus())) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Esta ordem não está aguardando aprovação.");
        }

        ordem.setStatus(aprovada ? "Ativa" : "Recusada");
        ordemManutencaoRepository.save(ordem);

        Notificacao n = new Notificacao();
        n.setUsuarioDestinatario(ordem.getAbertaPor());
        n.setTipo(aprovada ? "ordem_aprovada" : "ordem_recusada");
        n.setMensagem("Sua ordem de manutenção para a máquina " + ordem.getMaquina().getNome() + " foi " + (aprovada ? "aprovada" : "recusada") + ".");
        n.setLida(false);
        n.setDataCriacao(LocalDateTime.now());
        notificacaoRepository.save(n);

        return toDTO(ordem);
    }

    @Transactional
    public OrdemManutencaoDTO encerrarOrdem(Long idOrdem, String observacao) {
        if (observacao == null || observacao.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "A observação de encerramento é obrigatória.");
        }

        OrdemManutencao ordem = ordemManutencaoRepository.findById(idOrdem)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Ordem não encontrada."));

        if (!"Ativa".equals(ordem.getStatus())) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Apenas ordens ativas podem ser encerradas.");
        }

        ordem.setStatus("Encerrada");
        ordem.setObservacaoEncerramento(observacao);
        ordem.setDataEncerramento(LocalDateTime.now());

        ordem = ordemManutencaoRepository.save(ordem);

        classificacaoRiscoService.recalcularRisco(ordem.getMaquina());

        return toDTO(ordem);
    }

    @Transactional
    public void removerDaAba(Long idOrdem) {
        OrdemManutencao ordem = ordemManutencaoRepository.findById(idOrdem)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Ordem não encontrada."));

        if (!"Encerrada".equals(ordem.getStatus()) && !"Recusada".equals(ordem.getStatus())) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Apenas ordens encerradas ou recusadas podem ser removidas da aba.");
        }

        ordem.setRemovidaDaAba(true);
        ordemManutencaoRepository.save(ordem);
    }

    public List<OrdemManutencaoDTO> listarOrdens(Long idUsuario, String perfilUsuario) {
        if ("PROPRIETARIO".equals(perfilUsuario) || "SOCIO".equals(perfilUsuario)) {
            return ordemManutencaoRepository.findAll().stream()
                    .filter(o -> !o.isRemovidaDaAba())
                    .sorted((a, b) -> b.getDataAbertura().compareTo(a.getDataAbertura()))
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        }

        Usuario usuario = userRepository.findById(idUsuario).orElse(null);
        if (usuario == null) return List.of();

        List<Long> idsMaquinasVinculadas = usuario.getMaquinas().stream().map(Maquina::getId).toList();

        return ordemManutencaoRepository.findAll().stream()
                .filter(o -> !o.isRemovidaDaAba())
                .filter(o -> idsMaquinasVinculadas.contains(o.getMaquina().getId()))
                .sorted((a, b) -> b.getDataAbertura().compareTo(a.getDataAbertura()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private void gerarNotificacaoProprietario(String tipo, String mensagem) {
        List<Usuario> proprietarios = userRepository.findAll().stream()
                .filter(u -> "PROPRIETARIO".equals(u.getPerfil()))
                .toList();

        for (Usuario p : proprietarios) {
            Notificacao n = new Notificacao();
            n.setUsuarioDestinatario(p);
            n.setTipo(tipo);
            n.setMensagem(mensagem);
            n.setLida(false);
            n.setDataCriacao(LocalDateTime.now());
            notificacaoRepository.save(n);
        }
    }

    private OrdemManutencaoDTO toDTO(OrdemManutencao o) {
        OrdemManutencaoDTO dto = new OrdemManutencaoDTO();
        dto.setId(o.getId());
        dto.setIdMaquina(o.getMaquina().getId());
        dto.setNomeMaquina(o.getMaquina().getNome());
        dto.setIdSolicitante(o.getAbertaPor().getId_usuario());
        dto.setNomeSolicitante(o.getAbertaPor().getNome());
        dto.setStatus(o.getStatus());
        dto.setPrioridade(o.getPrioridade());
        dto.setDescricao(o.getDescricao());
        dto.setObservacaoEncerramento(o.getObservacaoEncerramento());
        dto.setDataAbertura(o.getDataAbertura());
        dto.setDataEncerramento(o.getDataEncerramento());
        return dto;
    }
}
