package com.main.frotaBackEnd.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "registro_operacao")
public class RegistroOperacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_operacao")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_maquina", nullable = false)
    private Maquina maquina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario operador;

    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_fim")
    private LocalDateTime dataFim;

    @Column(name = "hodometro_inicio", nullable = false)
    private BigDecimal hodometroInicio;

    @Column(name = "hodometro_fim")
    private BigDecimal hodometroFim;

    @Column(name = "peso_carregado")
    private BigDecimal pesoCarregado;

    private String observacoes;

    @Column(name = "peso_final")
    private BigDecimal pesoFinal;

    public RegistroOperacao() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Maquina getMaquina() {
        return maquina;
    }

    public void setMaquina(Maquina maquina) {
        this.maquina = maquina;
    }

    public Usuario getOperador() {
        return operador;
    }

    public void setOperador(Usuario operador) {
        this.operador = operador;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }

    public BigDecimal getHodometroInicio() {
        return hodometroInicio;
    }

    public void setHodometroInicio(BigDecimal hodometroInicio) {
        this.hodometroInicio = hodometroInicio;
    }

    public BigDecimal getHodometroFim() {
        return hodometroFim;
    }

    public void setHodometroFim(BigDecimal hodometroFim) {
        this.hodometroFim = hodometroFim;
    }

    public BigDecimal getPesoCarregado() {
        return pesoCarregado;
    }

    public void setPesoCarregado(BigDecimal pesoCarregado) {
        this.pesoCarregado = pesoCarregado;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public BigDecimal getPesoFinal() {
        return pesoFinal;
    }

    public void setPesoFinal(BigDecimal pesoFinal) {
        this.pesoFinal = pesoFinal;
    }
}
