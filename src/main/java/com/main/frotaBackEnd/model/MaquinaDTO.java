package com.main.frotaBackEnd.model;

import java.math.BigDecimal;

public class MaquinaDTO {
    private Long id;
    private Long idFazenda;
    private String nomeFazenda;
    private Long idTalhao;
    private String nomeTalhao;
    private String nome;
    private String tipo;
    private String marca;
    private String modelo;
    private int ano;
    private String numeroSerie;
    private String placa;
    private BigDecimal hodometroInicial;
    private BigDecimal capacidadeTanque;
    private String tipoCombustivel;
    private Integer intervaloTrocaOleoHoras;
    private Integer intervaloInspecaoHoras;
    private BigDecimal consumoMedio;
    private String status;
    private String nivelRisco;
    private String dataAquisicao;
    private BigDecimal valorAquisicao;
    private String fotoPath;
    private String observacoes;

    public MaquinaDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdFazenda() {
        return idFazenda;
    }

    public void setIdFazenda(Long idFazenda) {
        this.idFazenda = idFazenda;
    }

    public String getNomeFazenda() {
        return nomeFazenda;
    }

    public void setNomeFazenda(String nomeFazenda) {
        this.nomeFazenda = nomeFazenda;
    }

    public Long getIdTalhao() {
        return idTalhao;
    }

    public void setIdTalhao(Long idTalhao) {
        this.idTalhao = idTalhao;
    }

    public String getNomeTalhao() {
        return nomeTalhao;
    }

    public void setNomeTalhao(String nomeTalhao) {
        this.nomeTalhao = nomeTalhao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public BigDecimal getHodometroInicial() {
        return hodometroInicial;
    }

    public void setHodometroInicial(BigDecimal hodometroInicial) {
        this.hodometroInicial = hodometroInicial;
    }

    public BigDecimal getCapacidadeTanque() {
        return capacidadeTanque;
    }

    public void setCapacidadeTanque(BigDecimal capacidadeTanque) {
        this.capacidadeTanque = capacidadeTanque;
    }

    public String getTipoCombustivel() {
        return tipoCombustivel;
    }

    public void setTipoCombustivel(String tipoCombustivel) {
        this.tipoCombustivel = tipoCombustivel;
    }

    public Integer getIntervaloTrocaOleoHoras() {
        return intervaloTrocaOleoHoras;
    }

    public void setIntervaloTrocaOleoHoras(Integer intervaloTrocaOleoHoras) {
        this.intervaloTrocaOleoHoras = intervaloTrocaOleoHoras;
    }

    public Integer getIntervaloInspecaoHoras() {
        return intervaloInspecaoHoras;
    }

    public void setIntervaloInspecaoHoras(Integer intervaloInspecaoHoras) {
        this.intervaloInspecaoHoras = intervaloInspecaoHoras;
    }

    public BigDecimal getConsumoMedio() {
        return consumoMedio;
    }

    public void setConsumoMedio(BigDecimal consumoMedio) {
        this.consumoMedio = consumoMedio;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNivelRisco() {
        return nivelRisco;
    }

    public void setNivelRisco(String nivelRisco) {
        this.nivelRisco = nivelRisco;
    }

    public String getDataAquisicao() {
        return dataAquisicao;
    }

    public void setDataAquisicao(String dataAquisicao) {
        this.dataAquisicao = dataAquisicao;
    }

    public BigDecimal getValorAquisicao() {
        return valorAquisicao;
    }

    public void setValorAquisicao(BigDecimal valorAquisicao) {
        this.valorAquisicao = valorAquisicao;
    }

    public String getFotoPath() {
        return fotoPath;
    }

    public void setFotoPath(String fotoPath) {
        this.fotoPath = fotoPath;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
