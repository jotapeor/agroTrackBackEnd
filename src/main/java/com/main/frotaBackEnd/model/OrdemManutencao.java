package com.main.frotaBackEnd.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ordem_manutencao")
public class OrdemManutencao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ordem")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_maquina", nullable = false)
    private Maquina maquina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_solicitante", nullable = false)
    private Usuario abertaPor;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String prioridade;

    @Column(nullable = false)
    private String descricao;

    @Column(name = "data_abertura")
    private LocalDateTime dataAbertura;

    @Column(name = "data_encerramento")
    private LocalDateTime dataEncerramento;

    @Column(name = "observacao_encerramento")
    private String observacaoEncerramento;

    @Column(name = "removida_da_aba")
    private boolean removidaDaAba = false;

    public OrdemManutencao() {
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

    public Usuario getAbertaPor() {
        return abertaPor;
    }

    public void setAbertaPor(Usuario abertaPor) {
        this.abertaPor = abertaPor;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(String prioridade) {
        this.prioridade = prioridade;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(LocalDateTime dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public LocalDateTime getDataEncerramento() {
        return dataEncerramento;
    }

    public void setDataEncerramento(LocalDateTime dataEncerramento) {
        this.dataEncerramento = dataEncerramento;
    }

    public String getObservacaoEncerramento() {
        return observacaoEncerramento;
    }

    public void setObservacaoEncerramento(String observacaoEncerramento) {
        this.observacaoEncerramento = observacaoEncerramento;
    }

    public boolean isRemovidaDaAba() {
        return removidaDaAba;
    }

    public void setRemovidaDaAba(boolean removidaDaAba) {
        this.removidaDaAba = removidaDaAba;
    }
}
