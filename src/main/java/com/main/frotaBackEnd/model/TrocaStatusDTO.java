package com.main.frotaBackEnd.model;

import java.math.BigDecimal;

public class TrocaStatusDTO {
    private String novoStatus;
    private boolean confirmacao;
    private BigDecimal pesoCarregado;
    private BigDecimal hodometroFim;
    private String observacoes;

    public TrocaStatusDTO() {}

    public String getNovoStatus() { return novoStatus; }
    public void setNovoStatus(String novoStatus) { this.novoStatus = novoStatus; }
    public boolean isConfirmacao() { return confirmacao; }
    public void setConfirmacao(boolean confirmacao) { this.confirmacao = confirmacao; }
    public BigDecimal getPesoCarregado() { return pesoCarregado; }
    public void setPesoCarregado(BigDecimal pesoCarregado) { this.pesoCarregado = pesoCarregado; }
    public BigDecimal getHodometroFim() { return hodometroFim; }
    public void setHodometroFim(BigDecimal hodometroFim) { this.hodometroFim = hodometroFim; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}
