package com.main.frotaBackEnd.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AbastecimentoDTO {
    private LocalDateTime dataAbastecimento;
    private BigDecimal litros;
    private String tipoCombustivel;
    private BigDecimal hodometroAtual;

    public AbastecimentoDTO() {}

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
