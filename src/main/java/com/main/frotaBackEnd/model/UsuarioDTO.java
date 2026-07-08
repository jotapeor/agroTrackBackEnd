package com.main.frotaBackEnd.model;

public class UsuarioDTO {
    private Long id_usuario;
    private String nome;
    private String email;
    private String senha;
    private String perfil;
    private boolean ativo;
    private boolean primeiro_acesso = true;

    public UsuarioDTO() {
    }

    public UsuarioDTO(Long id_usuario, String nome, String email, String senha, String perfil, boolean ativo, boolean primeiro_acesso) {
        this.id_usuario = id_usuario;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.perfil = perfil;
        this.ativo = ativo;
        this.primeiro_acesso = primeiro_acesso;
    }

    public Long getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(Long id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public boolean isPrimeiro_acesso() {
        return primeiro_acesso;
    }

    public void setPrimeiro_acesso(boolean primeiro_acesso) {
        this.primeiro_acesso = primeiro_acesso;
    }
}
