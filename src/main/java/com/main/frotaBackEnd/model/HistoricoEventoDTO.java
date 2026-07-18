package com.main.frotaBackEnd.model;

import java.time.LocalDateTime;
import java.util.Map;

public class HistoricoEventoDTO {
    private String tipo;
    private LocalDateTime data;
    private String titulo;
    private String descricao;
    private String responsavel;
    private Map<String, Object> dadosExtras;

    public HistoricoEventoDTO() {
    }

    public HistoricoEventoDTO(String tipo, LocalDateTime data, String titulo, String descricao, String responsavel, Map<String, Object> dadosExtras) {
        this.tipo = tipo;
        this.data = data;
        this.titulo = titulo;
        this.descricao = descricao;
        this.responsavel = responsavel;
        this.dadosExtras = dadosExtras;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public Map<String, Object> getDadosExtras() {
        return dadosExtras;
    }

    public void setDadosExtras(Map<String, Object> dadosExtras) {
        this.dadosExtras = dadosExtras;
    }
}
