package com.main.frotaBackEnd.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TelemetriaDTO {
    private Long idMaquina;
    private String nomeMaquina;
    private String operadorAtual;
    private BigDecimal velocidadeAtual;
    private BigDecimal consumoAtual;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime dataAtualizacao;
    private String status;
    private boolean emOperacao;

    public TelemetriaDTO() {
    }

    public Long getIdMaquina() {
        return idMaquina;
    }

    public void setIdMaquina(Long idMaquina) {
        this.idMaquina = idMaquina;
    }

    public String getNomeMaquina() {
        return nomeMaquina;
    }

    public void setNomeMaquina(String nomeMaquina) {
        this.nomeMaquina = nomeMaquina;
    }

    public String getOperadorAtual() {
        return operadorAtual;
    }

    public void setOperadorAtual(String operadorAtual) {
        this.operadorAtual = operadorAtual;
    }

    public BigDecimal getVelocidadeAtual() {
        return velocidadeAtual;
    }

    public void setVelocidadeAtual(BigDecimal velocidadeAtual) {
        this.velocidadeAtual = velocidadeAtual;
    }

    public BigDecimal getConsumoAtual() {
        return consumoAtual;
    }

    public void setConsumoAtual(BigDecimal consumoAtual) {
        this.consumoAtual = consumoAtual;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isEmOperacao() {
        return emOperacao;
    }

    public void setEmOperacao(boolean emOperacao) {
        this.emOperacao = emOperacao;
    }
}
