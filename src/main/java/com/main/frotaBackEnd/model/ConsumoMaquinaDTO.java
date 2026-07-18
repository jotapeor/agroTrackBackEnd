package com.main.frotaBackEnd.model;

import java.math.BigDecimal;

public class ConsumoMaquinaDTO {
    private Long idMaquina;
    private String nomeMaquina;
    private BigDecimal consumoMedio;

    public ConsumoMaquinaDTO(Long idMaquina, String nomeMaquina, BigDecimal consumoMedio) {
        this.idMaquina = idMaquina;
        this.nomeMaquina = nomeMaquina;
        this.consumoMedio = consumoMedio;
    }

    public Long getIdMaquina() { return idMaquina; }
    public void setIdMaquina(Long idMaquina) { this.idMaquina = idMaquina; }
    public String getNomeMaquina() { return nomeMaquina; }
    public void setNomeMaquina(String nomeMaquina) { this.nomeMaquina = nomeMaquina; }
    public BigDecimal getConsumoMedio() { return consumoMedio; }
    public void setConsumoMedio(BigDecimal consumoMedio) { this.consumoMedio = consumoMedio; }
}
