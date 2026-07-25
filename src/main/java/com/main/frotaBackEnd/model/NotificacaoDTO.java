package com.main.frotaBackEnd.model;

import java.time.LocalDateTime;

public class NotificacaoDTO {
    private Long id;
    private String tipo;
    private String mensagem;
    private boolean lida;
    private LocalDateTime dataCriacao;
    private Long idOrdem;
    private String descricaoOrdem;
    private String prioridadeOrdem;
    private String statusOrdem;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public boolean isLida() { return lida; }
    public void setLida(boolean lida) { this.lida = lida; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public Long getIdOrdem() { return idOrdem; }
    public void setIdOrdem(Long idOrdem) { this.idOrdem = idOrdem; }
    public String getDescricaoOrdem() { return descricaoOrdem; }
    public void setDescricaoOrdem(String descricaoOrdem) { this.descricaoOrdem = descricaoOrdem; }
    public String getPrioridadeOrdem() { return prioridadeOrdem; }
    public void setPrioridadeOrdem(String prioridadeOrdem) { this.prioridadeOrdem = prioridadeOrdem; }
    public String getStatusOrdem() { return statusOrdem; }
    public void setStatusOrdem(String statusOrdem) { this.statusOrdem = statusOrdem; }
}
