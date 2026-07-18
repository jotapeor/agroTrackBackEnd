package com.main.frotaBackEnd.model;

public class AlertaTimelineDTO {
    private String data;
    private Long quantidade;
    private String tipo;

    public AlertaTimelineDTO(String data, Long quantidade, String tipo) {
        this.data = data;
        this.quantidade = quantidade;
        this.tipo = tipo;
    }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    public Long getQuantidade() { return quantidade; }
    public void setQuantidade(Long quantidade) { this.quantidade = quantidade; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
