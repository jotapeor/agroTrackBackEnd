package com.main.frotaBackEnd.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "maquina")
public class Maquina {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_maquina")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fazenda")
    private Fazenda fazenda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_talhao")
    private Talhao talhao;

    private String nome;
    private String tipo;
    private String marca;
    private String modelo;
    private int ano;

    @Column(name = "numero_serie")
    private String numeroSerie;

    private String placa;

    @Column(name = "hodometro_inicial")
    private BigDecimal hodometroInicial = BigDecimal.ZERO;

    @Column(name = "capacidade_tanque")
    private BigDecimal capacidadeTanque;

    @Column(name = "tipo_combustivel")
    private String tipoCombustivel;

    @Column(name = "intervalo_troca_oleo_horas")
    private Integer intervaloTrocaOleoHoras;

    @Column(name = "intervalo_inspecao_horas")
    private Integer intervaloInspecaoHoras;

    @Column(name = "consumo_medio")
    private BigDecimal consumoMedio;

    private String status = "Disponivel";

    @Column(name = "nivel_risco")
    private String nivelRisco = "Baixo";

    @Column(name = "data_aquisicao")
    private Date dataAquisicao;

    @Column(name = "valor_aquisicao")
    private BigDecimal valorAquisicao;

    @Column(name = "foto_path")
    private String fotoPath;

    private String observacoes;

    @Column(name = "data_cadastro")
    private Date dataCadastro;

    @JsonIgnore
    @ManyToMany(mappedBy = "maquinas")
    private List<Usuario> usuarios = new ArrayList<>();

    public Maquina() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Fazenda getFazenda() {
        return fazenda;
    }

    public void setFazenda(Fazenda fazenda) {
        this.fazenda = fazenda;
    }

    public Talhao getTalhao() {
        return talhao;
    }

    public void setTalhao(Talhao talhao) {
        this.talhao = talhao;
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

    public Date getDataAquisicao() {
        return dataAquisicao;
    }

    public void setDataAquisicao(Date dataAquisicao) {
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

    public Date getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(Date dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }
}
