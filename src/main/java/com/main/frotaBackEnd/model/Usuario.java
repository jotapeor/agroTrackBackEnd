package com.main.frotaBackEnd.model;

import jakarta.persistence.*;

import java.sql.Date;

@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id_usuario;

    private String nome;
    private String email;
    private String senha;
    private String perfil;
    private boolean ativo;

    @Column(name = "primeiro_acesso")
    private boolean primeiro_acesso = true;

    @Column(name = "data_criacao")
    private Date data_criacao;

    public Usuario() {
    }

    public Usuario(Long id_usuario, String nome, String email, String senha, String perfil, boolean ativo, boolean primeiro_acesso, Date data_criacao) {
        this.id_usuario = id_usuario;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.perfil = perfil;
        this.ativo = ativo;
        this.primeiro_acesso = primeiro_acesso;
        this.data_criacao = data_criacao;
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

    public Date getData_criacao() {
        return data_criacao;
    }

    public void setData_criacao(Date data_criacao) {
        this.data_criacao = data_criacao;
    }

    public boolean isPrimeiro_acesso() {
        return primeiro_acesso;
    }

    public void setPrimeiro_acesso(boolean primeiro_acesso) {
        this.primeiro_acesso = primeiro_acesso;
    }
}
