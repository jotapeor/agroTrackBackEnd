package com.main.frotaBackEnd.service;

import com.main.frotaBackEnd.model.*;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RelatorioService {

    @Autowired
    private EntityManager em;

    public List<ConsumoMaquinaDTO> relatorioConsumo(LocalDateTime inicio, LocalDateTime fim) {
        List<Object[]> results = em.createQuery(
            "SELECT m.id, m.nome, m.consumoMedio FROM Maquina m WHERE m.ativo = true AND m.consumoMedio IS NOT NULL ORDER BY m.consumoMedio DESC", Object[].class)
            .getResultList();

        return results.stream()
                .map(r -> new ConsumoMaquinaDTO((Long)r[0], (String)r[1], (BigDecimal)r[2]))
                .collect(Collectors.toList());
    }

    public Map<String, Long> relatorioRisco() {
        List<Object[]> results = em.createQuery(
            "SELECT m.nivelRisco, COUNT(m) FROM Maquina m WHERE m.ativo = true AND m.nivelRisco IS NOT NULL GROUP BY m.nivelRisco", Object[].class)
            .getResultList();

        Map<String, Long> map = new HashMap<>();
        for (Object[] r : results) {
            map.put((String)r[0], (Long)r[1]);
        }
        return map;
    }

    public Map<String, Long> relatorioOrdensPorStatus(LocalDateTime inicio, LocalDateTime fim) {
        List<Object[]> results = em.createQuery(
            "SELECT o.status, COUNT(o) FROM OrdemManutencao o WHERE o.dataAbertura >= :inicio AND o.dataAbertura <= :fim GROUP BY o.status", Object[].class)
            .setParameter("inicio", inicio)
            .setParameter("fim", fim)
            .getResultList();

        Map<String, Long> map = new HashMap<>();
        for (Object[] r : results) {
            map.put((String)r[0], (Long)r[1]);
        }
        return map;
    }

    public List<HorasKmDTO> relatorioHorasKm(LocalDateTime inicio, LocalDateTime fim, Long idMaquina, Long idOperador) {
        StringBuilder queryStr = new StringBuilder(
            "SELECT r FROM RegistroOperacao r JOIN FETCH r.maquina JOIN FETCH r.operador WHERE r.dataFim IS NOT NULL AND r.dataFim >= :inicio AND r.dataFim <= :fim"
        );
        if (idMaquina != null) {
            queryStr.append(" AND r.maquina.id = :idMaquina");
        }
        if (idOperador != null) {
            queryStr.append(" AND r.operador.id_usuario = :idOperador");
        }

        var query = em.createQuery(queryStr.toString(), RegistroOperacao.class)
                .setParameter("inicio", inicio)
                .setParameter("fim", fim);

        if (idMaquina != null) query.setParameter("idMaquina", idMaquina);
        if (idOperador != null) query.setParameter("idOperador", idOperador);

        List<RegistroOperacao> registros = query.getResultList();

        Map<String, HorasKmDTO> mapa = new HashMap<>();

        for (RegistroOperacao r : registros) {
            String key = r.getMaquina().getId() + "-" + r.getOperador().getId_usuario();
            HorasKmDTO dto = mapa.computeIfAbsent(key, k ->
                new HorasKmDTO(r.getMaquina().getId(), r.getMaquina().getNome(), r.getOperador().getId_usuario(), r.getOperador().getNome(), BigDecimal.ZERO, BigDecimal.ZERO)
            );

            long minutos = Duration.between(r.getDataInicio(), r.getDataFim()).toMinutes();
            BigDecimal horas = BigDecimal.valueOf(minutos).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            dto.setTotalHoras(dto.getTotalHoras().add(horas));

            if (r.getHodometroFim() != null && r.getHodometroInicio() != null) {
                BigDecimal km = r.getHodometroFim().subtract(r.getHodometroInicio());
                if (km.compareTo(BigDecimal.ZERO) > 0) {
                    dto.setTotalKm(dto.getTotalKm().add(km));
                }
            }
        }

        return new ArrayList<>(mapa.values());
    }

    public List<AlertaTimelineDTO> relatorioAlertasTimeline(LocalDateTime inicio, LocalDateTime fim) {
        String sql = "SELECT DATE(n.data_criacao) as data, n.tipo, COUNT(*) as quantidade " +
                     "FROM notificacao n " +
                     "WHERE n.data_criacao >= :inicio AND n.data_criacao <= :fim " +
                     "AND n.tipo IN ('alerta_preventivo', 'anomalia') " +
                     "GROUP BY DATE(n.data_criacao), n.tipo " +
                     "ORDER BY data";

        List<Object[]> results = em.createNativeQuery(sql)
                .setParameter("inicio", inicio)
                .setParameter("fim", fim)
                .getResultList();

        List<AlertaTimelineDTO> result = new ArrayList<>();
        for (Object[] r : results) {
            String data = r[0].toString();
            String tipo = (String) r[1];
            Long count = ((Number) r[2]).longValue();
            result.add(new AlertaTimelineDTO(data, count, tipo));
        }

        return result;
    }
}
