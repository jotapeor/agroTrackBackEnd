package com.main.frotaBackEnd.controller;

import com.main.frotaBackEnd.model.AlertaTimelineDTO;
import com.main.frotaBackEnd.model.ConsumoMaquinaDTO;
import com.main.frotaBackEnd.model.HorasKmDTO;
import com.main.frotaBackEnd.service.RelatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/relatorios")
@PreAuthorize("hasAnyRole('PROPRIETARIO', 'SOCIO')")
public class RelatorioController {

    @Autowired
    private RelatorioService relatorioService;

    private LocalDateTime parseDateOrDefault(String dateStr, boolean isEnd) {
        if (dateStr == null || dateStr.isEmpty()) {
            return isEnd ? LocalDateTime.now() : LocalDateTime.now().minusDays(30);
        }
        if (dateStr.length() == 10) {
            return isEnd ? LocalDateTime.parse(dateStr + "T23:59:59") : LocalDateTime.parse(dateStr + "T00:00:00");
        }
        return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
    }

    @GetMapping("/consumo-por-maquina")
    public List<ConsumoMaquinaDTO> relatorioConsumo(
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim) {
        LocalDateTime inicio = parseDateOrDefault(dataInicio, false);
        LocalDateTime fim = parseDateOrDefault(dataFim, true);
        return relatorioService.relatorioConsumo(inicio, fim);
    }

    @GetMapping("/risco-distribuicao")
    public Map<String, Long> relatorioRisco(
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim) {
        return relatorioService.relatorioRisco();
    }

    @GetMapping("/ordens-por-status")
    public Map<String, Long> ordensPorStatus(
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim) {
        LocalDateTime inicio = parseDateOrDefault(dataInicio, false);
        LocalDateTime fim = parseDateOrDefault(dataFim, true);
        return relatorioService.relatorioOrdensPorStatus(inicio, fim);
    }

    @GetMapping("/horas-km")
    public List<HorasKmDTO> horasKm(
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim,
            @RequestParam(required = false) Long idMaquina,
            @RequestParam(required = false) Long idOperador) {
        LocalDateTime inicio = parseDateOrDefault(dataInicio, false);
        LocalDateTime fim = parseDateOrDefault(dataFim, true);
        return relatorioService.relatorioHorasKm(inicio, fim, idMaquina, idOperador);
    }

    @GetMapping("/alertas-timeline")
    public List<AlertaTimelineDTO> alertasTimeline(
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim) {
        LocalDateTime inicio = parseDateOrDefault(dataInicio, false);
        LocalDateTime fim = parseDateOrDefault(dataFim, true);
        return relatorioService.relatorioAlertasTimeline(inicio, fim);
    }
}
