package com.main.frotaBackEnd.service;

import com.main.frotaBackEnd.model.*;
import com.main.frotaBackEnd.repository.AbastecimentoRepository;
import com.main.frotaBackEnd.repository.AutorizacaoRiscoRepository;
import com.main.frotaBackEnd.repository.HistoricoStatusMaquinaRepository;
import com.main.frotaBackEnd.repository.MaquinaRepository;
import com.main.frotaBackEnd.repository.RegistroOperacaoRepository;
import com.main.frotaBackEnd.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OperacaoService {

    @Autowired
    private MaquinaRepository maquinaRepository;

    @Autowired
    private RegistroOperacaoRepository registroOperacaoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassificacaoRiscoService classificacaoRiscoService;

    @Autowired
    private AbastecimentoService abastecimentoService;

    @Autowired
    private AbastecimentoRepository abastecimentoRepository;

    @Autowired
    private AutorizacaoRiscoRepository autorizacaoRiscoRepository;

    @Autowired
    private com.main.frotaBackEnd.repository.NotificacaoRepository notificacaoRepository;

    @Autowired
    private HistoricoStatusMaquinaRepository historicoStatusMaquinaRepository;

    @Transactional
    public RegistroOperacaoDTO trocarStatus(Long idMaquina, TrocaStatusDTO dto, Long idUsuarioLogado, String perfilUsuario) {
        Maquina maquina = maquinaRepository.findById(idMaquina)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Máquina não encontrada."));

        Usuario usuario = userRepository.findById(idUsuarioLogado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Usuário não encontrado."));

        if (!"PROPRIETARIO".equals(perfilUsuario) && !"SOCIO".equals(perfilUsuario)) {
            boolean vinculado = userRepository.verificaVinculo(idUsuarioLogado, idMaquina);
            if (!vinculado) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Você não tem permissão para operar esta máquina.");
            }
        }

        if (!"PROPRIETARIO".equals(perfilUsuario) && !"SOCIO".equals(perfilUsuario)) {
            List<RegistroOperacao> operacoesAtivas = registroOperacaoRepository.buscarOperacoesAtivas(idMaquina);
            if (!operacoesAtivas.isEmpty()) {
                RegistroOperacao opAberta = operacoesAtivas.get(0);
                if (!opAberta.getOperador().getId_usuario().equals(idUsuarioLogado)) {
                    throw new ResponseStatusException(HttpStatusCode.valueOf(403),
                        "Esta máquina já está em operação por outro colaborador.");
                }
            }
        }

        String statusAtual = maquina.getStatus();
        String novoStatus = dto.getNovoStatus();

        if (novoStatus.equals(statusAtual)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "A máquina já está neste status.");
        }

        if ("Em Manutencao".equals(statusAtual) && "Em Operacao".equals(novoStatus)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Não é possível entrar em operação enquanto a máquina está em manutenção.");
        }

        RegistroOperacaoDTO resumo = null;

        if ("Em Operacao".equals(novoStatus)) {
            if (!dto.isConfirmacao()) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Confirmação é obrigatória para iniciar a operação.");
            }
            if (("Pulverizador".equals(maquina.getTipo()) || "Semeadeira".equals(maquina.getTipo())) && dto.getPesoCarregado() == null) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Peso/volume carregado é obrigatório para pulverizadores e semeadeiras.");
            }

            if ("Alto".equals(maquina.getNivelRisco())) {
                if (!maquina.isAutorizadaOperacaoRisco()) {
                    List<Usuario> proprietarios = userRepository.findAll().stream()
                            .filter(u -> "PROPRIETARIO".equals(u.getPerfil())).toList();
                    LocalDateTime limite = LocalDateTime.now().minusHours(24);
                    for (Usuario p : proprietarios) {
                        List<Notificacao> recentes = notificacaoRepository.buscarPorMaquinaId(maquina.getId());
                        boolean jaExiste = recentes.stream().anyMatch(n ->
                                "bloqueio_risco".equals(n.getTipo()) &&
                                n.getUsuarioDestinatario().getId_usuario().equals(p.getId_usuario()) &&
                                n.getDataCriacao() != null && n.getDataCriacao().isAfter(limite));
                        if (!jaExiste) {
                            Notificacao notif = new Notificacao();
                            notif.setUsuarioDestinatario(p);
                            notif.setTipo("bloqueio_risco");
                            notif.setMensagem("A operação da máquina " + maquina.getNome() + " foi bloqueada por nível de risco ALTO. É necessária autorização prévia.");
                            notif.setMaquina(maquina);
                            notif.setLida(false);
                            notif.setDataCriacao(LocalDateTime.now());
                            notificacaoRepository.save(notif);
                        }
                    }
                    throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Máquina em nível de risco ALTO. É necessária autorização prévia do proprietário.");
                }
                maquina.setAutorizadaOperacaoRisco(false);
            }

            RegistroOperacao registro = new RegistroOperacao();
            registro.setMaquina(maquina);
            registro.setOperador(usuario);
            registro.setDataInicio(LocalDateTime.now());
            registro.setHodometroInicio(maquina.getHodometroInicial());
            registro.setPesoCarregado(dto.getPesoCarregado());

            registroOperacaoRepository.save(registro);

        } else if ("Em Operacao".equals(statusAtual)) {
            List<RegistroOperacao> ativas = registroOperacaoRepository.buscarOperacoesAtivas(idMaquina);
            if (!ativas.isEmpty()) {
                RegistroOperacao opAtiva = ativas.get(0);
                if (!opAtiva.getOperador().getId_usuario().equals(idUsuarioLogado)) {
                    throw new ResponseStatusException(HttpStatusCode.valueOf(403),
                        "Somente o operador que iniciou a operação pode encerrá-la.");
                }
            }

            if (dto.getHodometroFim() == null) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Hodômetro final é obrigatório ao encerrar uma operação.");
            }

            if (!ativas.isEmpty()) {
                RegistroOperacao registro = ativas.get(0);
                if (dto.getHodometroFim().compareTo(registro.getHodometroInicio()) < 0) {
                    throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Hodômetro final não pode ser menor que o hodômetro de início da operação.");
                }
                registro.setDataFim(LocalDateTime.now());
                registro.setHodometroFim(dto.getHodometroFim());
                registro.setObservacoes(dto.getObservacoes());

                if ("Colheitadeira".equals(maquina.getTipo()) && dto.getPesoFinal() != null) {
                    registro.setPesoFinal(dto.getPesoFinal());
                }

                registroOperacaoRepository.save(registro);

                maquina.setHodometroInicial(dto.getHodometroFim());

                resumo = toDTO(registro);

                List<Abastecimento> absPeriodo = abastecimentoRepository.buscarPorMaquinaEIntervalo(idMaquina, registro.getDataInicio(), registro.getDataFim());
                BigDecimal consumoEstimado = maquina.getConsumoMedio();
                if (absPeriodo != null && !absPeriodo.isEmpty() && dto.getHodometroFim() != null && registro.getHodometroInicio() != null) {
                    BigDecimal kmOperacao = dto.getHodometroFim().subtract(registro.getHodometroInicio());
                    if (kmOperacao.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal totalLitros = absPeriodo.stream().map(Abastecimento::getLitros).reduce(BigDecimal.ZERO, BigDecimal::add);
                        consumoEstimado = totalLitros.divide(kmOperacao, 2, RoundingMode.HALF_UP);
                    }
                }
                resumo.setConsumoEstimadoOperacao(consumoEstimado);

                List<Notificacao> nots = notificacaoRepository.buscarPorMaquinaEIntervalo(idMaquina, registro.getDataInicio(), registro.getDataFim());
                List<NotificacaoDTO> alertas = nots.stream().map(n -> {
                    NotificacaoDTO ndto = new NotificacaoDTO();
                    ndto.setId(n.getId());
                    ndto.setTipo(n.getTipo());
                    ndto.setMensagem(n.getMensagem());
                    ndto.setLida(n.isLida());
                    ndto.setDataCriacao(n.getDataCriacao());
                    return ndto;
                }).collect(Collectors.toList());
                resumo.setAlertasGerados(alertas);

                classificacaoRiscoService.recalcularRisco(maquina);
                abastecimentoService.verificarAlertaPreventivo(maquina, dto.getHodometroFim());
            }
        }

        maquina.setStatus(novoStatus);

        if ("Inativa".equals(novoStatus) || "Em Manutencao".equals(novoStatus)) {
            if (dto.getMotivo() == null || dto.getMotivo().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(400),
                    "O motivo é obrigatório ao mudar para " + novoStatus + ".");
            }
            HistoricoStatusMaquina hist = new HistoricoStatusMaquina();
            hist.setMaquina(maquina);
            hist.setUsuario(usuario);
            hist.setStatusAnterior(statusAtual);
            hist.setNovoStatus(novoStatus);
            hist.setMotivo(dto.getMotivo());
            hist.setDataAlteracao(LocalDateTime.now());
            historicoStatusMaquinaRepository.save(hist);
        }

        maquinaRepository.save(maquina);

        return resumo;
    }

    public List<Map<String, Object>> listarHistoricoMaquina(Long idMaquina) {
        List<Map<String, Object>> historico = new ArrayList<>();

        registroOperacaoRepository.buscarPorMaquinaId(idMaquina).forEach(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("tipo", "Operacao");
            map.put("id", r.getId());
            map.put("dataInicio", r.getDataInicio().toString());
            map.put("dataFim", r.getDataFim() != null ? r.getDataFim().toString() : null);
            map.put("nomeOperador", r.getOperador().getNome());
            map.put("observacoes", r.getObservacoes());
            if (r.getDataFim() != null) {
                long minutes = Duration.between(r.getDataInicio(), r.getDataFim()).toMinutes();
                map.put("horasOperadas", BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));
                if (r.getHodometroFim() != null && r.getHodometroInicio() != null) {
                    map.put("kmRodados", r.getHodometroFim().subtract(r.getHodometroInicio()));
                }
            }
            map.put("pesoCarregado", r.getPesoCarregado());
            map.put("pesoFinal", r.getPesoFinal());
            historico.add(map);
        });

        abastecimentoRepository.buscarPorMaquinaId(idMaquina).forEach(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("tipo", "Abastecimento");
            map.put("id", a.getId());
            map.put("dataAbastecimento", a.getDataAbastecimento().toString());
            map.put("litros", a.getLitros());
            map.put("tipoCombustivel", a.getTipoCombustivel());
            map.put("hodometroAtual", a.getHodometroAtual());
            map.put("nomeOperador", a.getOperador().getNome());
            historico.add(map);
        });

        autorizacaoRiscoRepository.buscarPorMaquinaId(idMaquina).forEach(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("tipo", "AutorizacaoRisco");
            map.put("id", a.getId());
            map.put("dataAutorizacao", a.getDataAutorizacao().toString());
            map.put("justificativa", a.getJustificativa());
            map.put("nomeAutorizador", a.getAutorizadoPor().getNome());
            historico.add(map);
        });

        historicoStatusMaquinaRepository.buscarPorMaquinaId(idMaquina).forEach(h -> {
            Map<String, Object> map = new HashMap<>();
            map.put("tipo", "MudancaStatus");
            map.put("id", h.getId());
            map.put("dataAlteracao", h.getDataAlteracao().toString());
            map.put("statusAnterior", h.getStatusAnterior());
            map.put("novoStatus", h.getNovoStatus());
            map.put("motivo", h.getMotivo());
            map.put("nomeUsuario", h.getUsuario().getNome());
            historico.add(map);
        });

        historico.sort((m1, m2) -> {
            String d1 = m1.containsKey("dataInicio") ? (String) m1.get("dataInicio") :
                        m1.containsKey("dataAbastecimento") ? (String) m1.get("dataAbastecimento") :
                        m1.containsKey("dataAlteracao") ? (String) m1.get("dataAlteracao") :
                        (String) m1.get("dataAutorizacao");
            String d2 = m2.containsKey("dataInicio") ? (String) m2.get("dataInicio") :
                        m2.containsKey("dataAbastecimento") ? (String) m2.get("dataAbastecimento") :
                        m2.containsKey("dataAlteracao") ? (String) m2.get("dataAlteracao") :
                        (String) m2.get("dataAutorizacao");
            if (d1 == null) return 1;
            if (d2 == null) return -1;
            return d2.compareTo(d1);
        });

        return historico;
    }

    public RegistroOperacaoDTO obterOperacaoAtiva(Long idMaquina) {
        List<RegistroOperacao> ativas = registroOperacaoRepository.buscarOperacoesAtivas(idMaquina);
        if (ativas.isEmpty()) {
            return null;
        }
        RegistroOperacao registro = ativas.get(0);
        RegistroOperacaoDTO dto = new RegistroOperacaoDTO();
        dto.setId(registro.getId());
        dto.setIdMaquina(registro.getMaquina().getId());
        dto.setNomeMaquina(registro.getMaquina().getNome());
        dto.setIdOperador(registro.getOperador().getId_usuario());
        dto.setNomeOperador(registro.getOperador().getNome());
        dto.setDataInicio(registro.getDataInicio());
        dto.setHodometroInicio(registro.getHodometroInicio());
        dto.setPesoCarregado(registro.getPesoCarregado());

        long minutes = Duration.between(registro.getDataInicio(), LocalDateTime.now()).toMinutes();
        dto.setHorasOperadas(BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));

        List<Abastecimento> absPeriodo = abastecimentoRepository.buscarPorMaquinaEIntervalo(
                idMaquina, registro.getDataInicio(), LocalDateTime.now());
        if (absPeriodo != null && !absPeriodo.isEmpty()) {
            BigDecimal totalLitros = absPeriodo.stream()
                    .map(Abastecimento::getLitros)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setConsumoEstimadoOperacao(totalLitros);
        }

        return dto;
    }

    private RegistroOperacaoDTO toDTO(RegistroOperacao r) {
        RegistroOperacaoDTO dto = new RegistroOperacaoDTO();
        dto.setId(r.getId());
        dto.setIdMaquina(r.getMaquina().getId());
        dto.setNomeMaquina(r.getMaquina().getNome());
        dto.setIdOperador(r.getOperador().getId_usuario());
        dto.setNomeOperador(r.getOperador().getNome());
        dto.setDataInicio(r.getDataInicio());
        dto.setDataFim(r.getDataFim());
        dto.setHodometroInicio(r.getHodometroInicio());
        dto.setHodometroFim(r.getHodometroFim());
        dto.setPesoCarregado(r.getPesoCarregado());
        dto.setPesoFinal(r.getPesoFinal());
        dto.setObservacoes(r.getObservacoes());

        if (r.getDataFim() != null) {
            long minutes = Duration.between(r.getDataInicio(), r.getDataFim()).toMinutes();
            dto.setHorasOperadas(BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));
            if (r.getHodometroFim() != null) {
                dto.setKmRodados(r.getHodometroFim().subtract(r.getHodometroInicio()));
            }
        }
        return dto;
    }
}
