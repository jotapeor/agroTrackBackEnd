package com.main.frotaBackEnd.service;

import com.main.frotaBackEnd.model.Maquina;
import com.main.frotaBackEnd.model.RegistroOperacao;
import com.main.frotaBackEnd.model.TelemetriaMaquina;
import com.main.frotaBackEnd.repository.MaquinaRepository;
import com.main.frotaBackEnd.repository.RegistroOperacaoRepository;
import com.main.frotaBackEnd.repository.TelemetriaMaquinaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class TelemetriaSimuladorService {

    @Autowired
    private MaquinaRepository maquinaRepository;

    @Autowired
    private TelemetriaMaquinaRepository telemetriaMaquinaRepository;

    @Autowired
    private RegistroOperacaoRepository registroOperacaoRepository;

    private final Random random = new Random();

    @Scheduled(fixedRate = 8000)
    @Transactional
    public void simularTelemetria() {
        List<Maquina> maquinas = maquinaRepository.findAll();

        for (Maquina maquina : maquinas) {
            if ("Em Operacao".equals(maquina.getStatus())) {
                gerarTelemetriaParaMaquina(maquina);
            }
        }
    }

    private void gerarTelemetriaParaMaquina(Maquina maquina) {
        Optional<TelemetriaMaquina> ultimaTelemetriaOpt = telemetriaMaquinaRepository.findTopByMaquinaIdOrderByDataAtualizacaoDesc(maquina.getId());

        BigDecimal lat;
        BigDecimal lon;

        if (ultimaTelemetriaOpt.isPresent()) {
            TelemetriaMaquina ultima = ultimaTelemetriaOpt.get();

            double deltaLat = (random.nextDouble() - 0.5) * 0.0006;
            double deltaLon = (random.nextDouble() - 0.5) * 0.0006;
            lat = ultima.getLatitude().add(BigDecimal.valueOf(deltaLat));
            lon = ultima.getLongitude().add(BigDecimal.valueOf(deltaLon));
        } else {

            lat = BigDecimal.valueOf(-12.9716 + (random.nextDouble() - 0.5) * 0.1);
            lon = BigDecimal.valueOf(-55.9129 + (random.nextDouble() - 0.5) * 0.1);
        }

        BigDecimal velocidade = simularVelocidade(maquina.getTipo());
        BigDecimal consumo = simularConsumo(maquina, velocidade);

        TelemetriaMaquina telemetria = ultimaTelemetriaOpt.orElse(new TelemetriaMaquina());
        telemetria.setMaquina(maquina);
        telemetria.setLatitude(lat.setScale(8, RoundingMode.HALF_UP));
        telemetria.setLongitude(lon.setScale(8, RoundingMode.HALF_UP));
        telemetria.setVelocidadeAtual(velocidade.setScale(2, RoundingMode.HALF_UP));
        telemetria.setConsumoAtual(consumo.setScale(2, RoundingMode.HALF_UP));
        telemetria.setDataAtualizacao(LocalDateTime.now());

        telemetriaMaquinaRepository.save(telemetria);
    }

    private BigDecimal simularVelocidade(String tipoMaquina) {
        double min = 0;
        double max = 20;

        if ("Trator".equalsIgnoreCase(tipoMaquina)) {
            min = 5; max = 15;
        } else if ("Colheitadeira".equalsIgnoreCase(tipoMaquina)) {
            min = 3; max = 8;
        } else if ("Pulverizador".equalsIgnoreCase(tipoMaquina) || "Semeadeira".equalsIgnoreCase(tipoMaquina)) {
            min = 6; max = 12;
        }

        double val = min + (max - min) * random.nextDouble();
        return BigDecimal.valueOf(val);
    }

    private BigDecimal simularConsumo(Maquina maquina, BigDecimal velocidade) {
        BigDecimal base = maquina.getConsumoMedio();
        if (base == null || base.compareTo(BigDecimal.ZERO) == 0) {
            base = BigDecimal.valueOf(15.0);
        }

        double variacao = 1.0 + (random.nextDouble() - 0.5) * 0.3;
        BigDecimal consumoCalc = base.multiply(BigDecimal.valueOf(variacao));

        List<RegistroOperacao> ativas = registroOperacaoRepository.buscarOperacoesAtivas(maquina.getId());
        if (!ativas.isEmpty()) {
            RegistroOperacao op = ativas.get(0);
            if (op.getPesoCarregado() != null && op.getPesoCarregado().compareTo(BigDecimal.ZERO) > 0) {

                consumoCalc = consumoCalc.multiply(BigDecimal.valueOf(1.2));
            }
        }

        return consumoCalc;
    }
}
