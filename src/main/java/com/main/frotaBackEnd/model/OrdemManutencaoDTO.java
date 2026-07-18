package com.main.frotaBackEnd.model;

import java.time.LocalDateTime;

public class OrdemManutencaoDTO {
    private Long id;
    private Long idMaquina;
    private String nomeMaquina;
    private Long idSolicitante;
    private String nomeSolicitante;
    private String status;
    private String prioridade;
    private String descricao;
    private String observacaoEncerramento;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataEncerramento;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdMaquina() { return idMaquina; }
    public void setIdMaquina(Long idMaquina) { this.idMaquina = idMaquina; }
    public String getNomeMaquina() { return nomeMaquina; }
    public void setNomeMaquina(String nomeMaquina) { this.nomeMaquina = nomeMaquina; }
    public Long getIdSolicitante() { return idSolicitante; }
    public void setIdSolicitante(Long idSolicitante) { this.idSolicitante = idSolicitante; }
    public String getNomeSolicitante() { return nomeSolicitante; }
    public void setNomeSolicitante(String nomeSolicitante) { this.nomeSolicitante = nomeSolicitante; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPrioridade() { return prioridade; }
    public void setPrioridade(String prioridade) { this.prioridade = prioridade; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getObservacaoEncerramento() { return observacaoEncerramento; }
    public void setObservacaoEncerramento(String observacaoEncerramento) { this.observacaoEncerramento = observacaoEncerramento; }
    public LocalDateTime getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDateTime dataAbertura) { this.dataAbertura = dataAbertura; }
    public LocalDateTime getDataEncerramento() { return dataEncerramento; }
    public void setDataEncerramento(LocalDateTime dataEncerramento) { this.dataEncerramento = dataEncerramento; }
}
