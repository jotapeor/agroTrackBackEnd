package com.main.frotaBackEnd.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RegistroOperacaoDTO {
    private Long id;
    private Long idMaquina;
    private String nomeMaquina;
    private Long idOperador;
    private String nomeOperador;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private BigDecimal hodometroInicio;
    private BigDecimal hodometroFim;
    private BigDecimal pesoCarregado;
    private String observacoes;
    private BigDecimal horasOperadas;
    private BigDecimal kmRodados;
    private BigDecimal consumoEstimadoOperacao;
    private java.util.List<NotificacaoDTO> alertasGerados;

    public RegistroOperacaoDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdMaquina() { return idMaquina; }
    public void setIdMaquina(Long idMaquina) { this.idMaquina = idMaquina; }
    public String getNomeMaquina() { return nomeMaquina; }
    public void setNomeMaquina(String nomeMaquina) { this.nomeMaquina = nomeMaquina; }
    public Long getIdOperador() { return idOperador; }
    public void setIdOperador(Long idOperador) { this.idOperador = idOperador; }
    public String getNomeOperador() { return nomeOperador; }
    public void setNomeOperador(String nomeOperador) { this.nomeOperador = nomeOperador; }
    public LocalDateTime getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDateTime dataInicio) { this.dataInicio = dataInicio; }
    public LocalDateTime getDataFim() { return dataFim; }
    public void setDataFim(LocalDateTime dataFim) { this.dataFim = dataFim; }
    public BigDecimal getHodometroInicio() { return hodometroInicio; }
    public void setHodometroInicio(BigDecimal hodometroInicio) { this.hodometroInicio = hodometroInicio; }
    public BigDecimal getHodometroFim() { return hodometroFim; }
    public void setHodometroFim(BigDecimal hodometroFim) { this.hodometroFim = hodometroFim; }
    public BigDecimal getPesoCarregado() { return pesoCarregado; }
    public void setPesoCarregado(BigDecimal pesoCarregado) { this.pesoCarregado = pesoCarregado; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public BigDecimal getHorasOperadas() { return horasOperadas; }
    public void setHorasOperadas(BigDecimal horasOperadas) { this.horasOperadas = horasOperadas; }
    public BigDecimal getKmRodados() { return kmRodados; }
    public void setKmRodados(BigDecimal kmRodados) { this.kmRodados = kmRodados; }
    public BigDecimal getConsumoEstimadoOperacao() { return consumoEstimadoOperacao; }
    public void setConsumoEstimadoOperacao(BigDecimal consumoEstimadoOperacao) { this.consumoEstimadoOperacao = consumoEstimadoOperacao; }
    public java.util.List<NotificacaoDTO> getAlertasGerados() { return alertasGerados; }
    public void setAlertasGerados(java.util.List<NotificacaoDTO> alertasGerados) { this.alertasGerados = alertasGerados; }
}
