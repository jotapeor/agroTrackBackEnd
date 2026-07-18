package com.main.frotaBackEnd.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "abastecimento")
public class Abastecimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_abastecimento")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_maquina", nullable = false)
    private Maquina maquina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario operador;

    @Column(name = "data_abastecimento")
    private LocalDateTime dataAbastecimento;

    @Column(nullable = false)
    private BigDecimal litros;

    @Column(name = "tipo_combustivel", nullable = false)
    private String tipoCombustivel;

    @Column(name = "hodometro_atual", nullable = false)
    private BigDecimal hodometroAtual;

    public Abastecimento() {
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

    public LocalDateTime getDataAbastecimento() {
        return dataAbastecimento;
    }

    public void setDataAbastecimento(LocalDateTime dataAbastecimento) {
        this.dataAbastecimento = dataAbastecimento;
    }

    public BigDecimal getLitros() {
        return litros;
    }

    public void setLitros(BigDecimal litros) {
        this.litros = litros;
    }

    public String getTipoCombustivel() {
        return tipoCombustivel;
    }

    public void setTipoCombustivel(String tipoCombustivel) {
        this.tipoCombustivel = tipoCombustivel;
    }

    public BigDecimal getHodometroAtual() {
        return hodometroAtual;
    }

    public void setHodometroAtual(BigDecimal hodometroAtual) {
        this.hodometroAtual = hodometroAtual;
    }
}
