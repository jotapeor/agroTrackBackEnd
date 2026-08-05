package com.main.frotaBackEnd.helper;

import com.main.frotaBackEnd.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TestDataFactory {

    public static Usuario criarUsuario(Long id, String nome, String email, String perfil) {
        Usuario u = new Usuario();
        u.setId_usuario(id);
        u.setNome(nome);
        u.setEmail(email);
        u.setSenha("hashed_password");
        u.setPerfil(perfil);
        u.setAtivo(true);
        u.setPrimeiro_acesso(false);
        return u;
    }

    public static Maquina criarMaquina(Long id, String nome, String status, String tipo, String nivelRisco) {
        Maquina m = new Maquina();
        m.setId(id);
        m.setNome(nome);
        m.setStatus(status);
        m.setTipo(tipo);
        m.setNivelRisco(nivelRisco);
        m.setAtivo(true);
        m.setHodometroInicial(new BigDecimal("1000"));
        m.setModelo("Modelo Test");
        m.setAno(2022);
        return m;
    }

    public static RegistroOperacao criarRegistroOperacaoAtivo(Maquina m, Usuario operador) {
        RegistroOperacao r = new RegistroOperacao();
        r.setMaquina(m);
        r.setOperador(operador);
        r.setDataInicio(LocalDateTime.now());
        r.setHodometroInicio(new BigDecimal("1000"));
        return r;
    }

    public static RegistroOperacao criarRegistroOperacaoEncerrado(Maquina m, Usuario operador) {
        RegistroOperacao r = new RegistroOperacao();
        r.setMaquina(m);
        r.setOperador(operador);
        r.setDataInicio(LocalDateTime.now().minusHours(2));
        r.setDataFim(LocalDateTime.now());
        r.setHodometroInicio(new BigDecimal("1000"));
        r.setHodometroFim(new BigDecimal("1100"));
        return r;
    }

    public static OrdemManutencao criarOrdemManutencao(Long id, Maquina m, Usuario abertaPor, String status) {
        OrdemManutencao o = new OrdemManutencao();
        o.setId(id);
        o.setMaquina(m);
        o.setAbertaPor(abertaPor);
        o.setStatus(status);
        o.setPrioridade("Baixa");
        o.setDescricao("Descrição de teste");
        o.setDataAbertura(LocalDateTime.now());
        return o;
    }

    public static TrocaStatusDTO criarTrocaStatusDTO(String novoStatus, BigDecimal hodometroFim,
                                                      String observacoes, String motivo) {
        TrocaStatusDTO dto = new TrocaStatusDTO();
        dto.setNovoStatus(novoStatus);
        dto.setHodometroFim(hodometroFim);
        dto.setObservacoes(observacoes);
        dto.setMotivo(motivo);
        return dto;
    }

    public static TrocaStatusDTO criarTrocaStatusDTOIniciar(boolean confirmacao, BigDecimal pesoCarregado) {
        TrocaStatusDTO dto = new TrocaStatusDTO();
        dto.setNovoStatus("Em Operacao");
        dto.setConfirmacao(confirmacao);
        dto.setPesoCarregado(pesoCarregado);
        return dto;
    }
}
