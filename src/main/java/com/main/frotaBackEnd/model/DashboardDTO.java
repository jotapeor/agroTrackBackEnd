package com.main.frotaBackEnd.model;

import java.util.List;
import java.util.Map;

public class DashboardDTO {
    private Map<String, Long> maquinasPorStatus;
    private long alertasAtivos;
    private long maquinasRiscoAlto;
    private List<NotificacaoDTO> notificacoesRecentes;
    private Map<String, Object> operacaoAtiva;

    public Map<String, Long> getMaquinasPorStatus() { return maquinasPorStatus; }
    public void setMaquinasPorStatus(Map<String, Long> maquinasPorStatus) { this.maquinasPorStatus = maquinasPorStatus; }
    public long getAlertasAtivos() { return alertasAtivos; }
    public void setAlertasAtivos(long alertasAtivos) { this.alertasAtivos = alertasAtivos; }
    public long getMaquinasRiscoAlto() { return maquinasRiscoAlto; }
    public void setMaquinasRiscoAlto(long maquinasRiscoAlto) { this.maquinasRiscoAlto = maquinasRiscoAlto; }
    public List<NotificacaoDTO> getNotificacoesRecentes() { return notificacoesRecentes; }
    public void setNotificacoesRecentes(List<NotificacaoDTO> notificacoesRecentes) { this.notificacoesRecentes = notificacoesRecentes; }
    public Map<String, Object> getOperacaoAtiva() { return operacaoAtiva; }
    public void setOperacaoAtiva(Map<String, Object> operacaoAtiva) { this.operacaoAtiva = operacaoAtiva; }
}
