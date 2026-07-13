package com.main.frotaBackEnd.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "talhao")
public class Talhao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_talhao")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fazenda", nullable = false)
    private Fazenda fazenda;

    private String nome;

    @Column(name = "area_hectares")
    private BigDecimal areaHectares;

    @Column(name = "cultura_atual")
    private String culturaAtual;

    private boolean ativo = true;

    public Talhao() {
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

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getAreaHectares() {
        return areaHectares;
    }

    public void setAreaHectares(BigDecimal areaHectares) {
        this.areaHectares = areaHectares;
    }

    public String getCulturaAtual() {
        return culturaAtual;
    }

    public void setCulturaAtual(String culturaAtual) {
        this.culturaAtual = culturaAtual;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
