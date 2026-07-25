package com.main.frotaBackEnd.model;

import jakarta.persistence.*;

@Entity
@Table(name = "maquina_combustivel")
public class MaquinaCombustivel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_maquina", nullable = false)
    private Maquina maquina;

    @Column(name = "tipo_combustivel", nullable = false)
    private String tipoCombustivel;

    public MaquinaCombustivel() {}

    public MaquinaCombustivel(Maquina maquina, String tipoCombustivel) {
        this.maquina = maquina;
        this.tipoCombustivel = tipoCombustivel;
    }

    public Long getId() { return id; }
    public Maquina getMaquina() { return maquina; }
    public void setMaquina(Maquina maquina) { this.maquina = maquina; }
    public String getTipoCombustivel() { return tipoCombustivel; }
    public void setTipoCombustivel(String tipoCombustivel) { this.tipoCombustivel = tipoCombustivel; }
}
