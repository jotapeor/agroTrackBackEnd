package com.main.frotaBackEnd.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "telemetria_maquina")
public class TelemetriaMaquina {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_maquina", nullable = false)
    private Maquina maquina;

    @Column(name = "velocidade_atual")
    private BigDecimal velocidadeAtual;

    @Column(name = "consumo_atual")
    private BigDecimal consumoAtual;

    @Column(precision = 11, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    public TelemetriaMaquina() {
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
}
