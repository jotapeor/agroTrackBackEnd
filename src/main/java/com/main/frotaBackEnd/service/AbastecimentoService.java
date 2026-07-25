package com.main.frotaBackEnd.service;

import com.main.frotaBackEnd.model.*;
import com.main.frotaBackEnd.repository.AbastecimentoRepository;
import com.main.frotaBackEnd.repository.MaquinaRepository;
import com.main.frotaBackEnd.repository.NotificacaoRepository;
import com.main.frotaBackEnd.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AbastecimentoService {

    @Autowired
    private AbastecimentoRepository abastecimentoRepository;

    @Autowired
    private MaquinaRepository maquinaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private com.main.frotaBackEnd.repository.RegistroOperacaoRepository registroOperacaoRepository;

    @Transactional
    public Abastecimento registrarAbastecimento(Long idMaquina, AbastecimentoDTO dto, Long idUsuarioLogado, String perfilUsuario) {
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

        if (dto.getHodometroAtual().compareTo(maquina.getHodometroInicial()) < 0) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Hodômetro informado não pode ser menor que o último registrado.");
        }

        Abastecimento abastecimento = new Abastecimento();
        abastecimento.setMaquina(maquina);
        abastecimento.setOperador(usuario);
        abastecimento.setDataAbastecimento(dto.getDataAbastecimento() != null ? dto.getDataAbastecimento() : LocalDateTime.now());
        abastecimento.setLitros(dto.getLitros());
        abastecimento.setTipoCombustivel(dto.getTipoCombustivel());
        abastecimento.setHodometroAtual(dto.getHodometroAtual());

        abastecimento = abastecimentoRepository.save(abastecimento);

        recalcularConsumo(maquina, abastecimento);

        maquina.setHodometroInicial(dto.getHodometroAtual());
        verificarAlertaPreventivo(maquina, dto.getHodometroAtual());

        maquinaRepository.save(maquina);

        return abastecimento;
    }

    private void recalcularConsumo(Maquina maquina, Abastecimento abastecimentoAtual) {
        List<Abastecimento> historico = abastecimentoRepository.buscarPorMaquinaId(maquina.getId());
        if (historico.size() < 2) return;

        Abastecimento anterior = historico.get(1);
        BigDecimal kmRodados = abastecimentoAtual.getHodometroAtual().subtract(anterior.getHodometroAtual());

        if (kmRodados.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal novoConsumo = abastecimentoAtual.getLitros().divide(kmRodados, 2, RoundingMode.HALF_UP);

            if (maquina.getConsumoMedio() != null && maquina.getConsumoMedio().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal mediaHistorica = maquina.getConsumoMedio();
                BigDecimal desvio = novoConsumo.subtract(mediaHistorica).abs().divide(mediaHistorica, 2, RoundingMode.HALF_UP);

                List<RegistroOperacao> operacoes = registroOperacaoRepository.buscarPorMaquinaIdEIntervalo(
                        maquina.getId(), anterior.getDataAbastecimento(), abastecimentoAtual.getDataAbastecimento());

                BigDecimal maxPeso = BigDecimal.ZERO;
                for (RegistroOperacao op : operacoes) {
                    if (op.getPesoCarregado() != null && op.getPesoCarregado().compareTo(maxPeso) > 0) {
                        maxPeso = op.getPesoCarregado();
                    }
                }

                BigDecimal capacidade = maquina.getCapacidadeTanque();
                if (capacidade == null || capacidade.compareTo(BigDecimal.ZERO) <= 0) {
                    capacidade = new BigDecimal("1000.0");
                }

                BigDecimal proporcaoPeso = maxPeso.divide(capacidade, 4, RoundingMode.HALF_UP);
                BigDecimal ajuste = proporcaoPeso.multiply(new BigDecimal("0.20"));
                BigDecimal limiteAjustado = new BigDecimal("0.30").add(ajuste);

                if (desvio.compareTo(limiteAjustado) > 0) {
                    gerarNotificacaoProprietario("anomalia", "Anomalia de consumo detectada na máquina " + maquina.getNome() + ". Consumo atual: " + novoConsumo + ", Média histórica: " + mediaHistorica + " (Desvio max ajustado: " + limiteAjustado.multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP) + "%).");
                }
            }

            maquina.setConsumoMedio(novoConsumo);
        }
    }

    public void verificarAlertaPreventivo(Maquina maquina, BigDecimal hodometroAtual) {
        if (maquina.getIntervaloTrocaOleoHoras() != null && maquina.getIntervaloTrocaOleoHoras() > 0) {
            BigDecimal intervalo = new BigDecimal(maquina.getIntervaloTrocaOleoHoras());
            BigDecimal fator = hodometroAtual.divide(intervalo, 0, RoundingMode.FLOOR);
            BigDecimal ultimoPonto = fator.multiply(intervalo);
            BigDecimal proxima = ultimoPonto.add(intervalo);

            if (proxima.subtract(hodometroAtual).compareTo(new BigDecimal("20")) <= 0) {
                gerarNotificacaoProprietario("alerta_preventivo", "Máquina " + maquina.getNome() + " próxima da troca de óleo. Hodômetro: " + hodometroAtual, maquina);
            }
        }
        if (maquina.getIntervaloInspecaoHoras() != null && maquina.getIntervaloInspecaoHoras() > 0) {
            BigDecimal intervalo = new BigDecimal(maquina.getIntervaloInspecaoHoras());
            BigDecimal fator = hodometroAtual.divide(intervalo, 0, RoundingMode.FLOOR);
            BigDecimal ultimoPonto = fator.multiply(intervalo);
            BigDecimal proxima = ultimoPonto.add(intervalo);

            if (proxima.subtract(hodometroAtual).compareTo(new BigDecimal("20")) <= 0) {
                gerarNotificacaoProprietario("alerta_preventivo", "Máquina " + maquina.getNome() + " próxima da inspeção. Hodômetro: " + hodometroAtual, maquina);
            }
        }
    }

    private void gerarNotificacaoProprietario(String tipo, String mensagem) {
        gerarNotificacaoProprietario(tipo, mensagem, null);
    }

    private void gerarNotificacaoProprietario(String tipo, String mensagem, Maquina maquina) {
        List<Usuario> proprietarios = userRepository.findAll().stream()
                .filter(u -> "PROPRIETARIO".equals(u.getPerfil()))
                .toList();

        LocalDateTime limite = LocalDateTime.now().minusHours(24);

        for (Usuario p : proprietarios) {
            if (maquina != null) {
                List<Notificacao> recentes = notificacaoRepository.buscarPorMaquinaId(maquina.getId());
                boolean jaExiste = recentes.stream().anyMatch(n ->
                    tipo.equals(n.getTipo()) &&
                    n.getUsuarioDestinatario().getId_usuario().equals(p.getId_usuario()) &&
                    n.getDataCriacao() != null && n.getDataCriacao().isAfter(limite));
                if (jaExiste) continue;
            }
            Notificacao n = new Notificacao();
            n.setUsuarioDestinatario(p);
            n.setTipo(tipo);
            n.setMensagem(mensagem);
            n.setMaquina(maquina);
            n.setLida(false);
            n.setDataCriacao(LocalDateTime.now());
            notificacaoRepository.save(n);
        }
    }
}
