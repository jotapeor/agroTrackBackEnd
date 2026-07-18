package com.main.frotaBackEnd.model;

import java.math.BigDecimal;

public class HorasKmDTO {
    private Long idMaquina;
    private String nomeMaquina;
    private Long idOperador;
    private String nomeOperador;
    private BigDecimal totalHoras;
    private BigDecimal totalKm;

    public HorasKmDTO() {}

    public HorasKmDTO(Long idMaquina, String nomeMaquina, Long idOperador, String nomeOperador, BigDecimal totalHoras, BigDecimal totalKm) {
        this.idMaquina = idMaquina;
        this.nomeMaquina = nomeMaquina;
        this.idOperador = idOperador;
        this.nomeOperador = nomeOperador;
        this.totalHoras = totalHoras;
        this.totalKm = totalKm;
    }

    public Long getIdMaquina() { return idMaquina; }
    public void setIdMaquina(Long idMaquina) { this.idMaquina = idMaquina; }
    public String getNomeMaquina() { return nomeMaquina; }
    public void setNomeMaquina(String nomeMaquina) { this.nomeMaquina = nomeMaquina; }
    public Long getIdOperador() { return idOperador; }
    public void setIdOperador(Long idOperador) { this.idOperador = idOperador; }
    public String getNomeOperador() { return nomeOperador; }
    public void setNomeOperador(String nomeOperador) { this.nomeOperador = nomeOperador; }
    public BigDecimal getTotalHoras() { return totalHoras; }
    public void setTotalHoras(BigDecimal totalHoras) { this.totalHoras = totalHoras; }
    public BigDecimal getTotalKm() { return totalKm; }
    public void setTotalKm(BigDecimal totalKm) { this.totalKm = totalKm; }
}
