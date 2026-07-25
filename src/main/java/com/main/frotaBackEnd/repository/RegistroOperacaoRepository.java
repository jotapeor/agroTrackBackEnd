package com.main.frotaBackEnd.repository;

import com.main.frotaBackEnd.model.RegistroOperacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistroOperacaoRepository extends JpaRepository<RegistroOperacao, Long> {

    @Query("select r from RegistroOperacao r where r.maquina.id = ?1 order by r.dataInicio desc")
    List<RegistroOperacao> buscarPorMaquinaId(Long maquinaId);

    @Query("select r from RegistroOperacao r where r.operador.id_usuario = ?1 order by r.dataInicio desc")
    List<RegistroOperacao> buscarPorOperadorId(Long operadorId);

    @Query("select r from RegistroOperacao r where r.maquina.id = ?1 and r.dataFim is null order by r.dataInicio desc")
    List<RegistroOperacao> buscarOperacoesAtivas(Long maquinaId);

    @Query("select r from RegistroOperacao r where r.maquina.id = ?1 and r.dataInicio >= ?2 and r.dataInicio <= ?3")
    List<RegistroOperacao> buscarPorMaquinaIdEIntervalo(Long maquinaId, java.time.LocalDateTime inicio, java.time.LocalDateTime fim);

    @Query("select r from RegistroOperacao r where r.maquina.id in ?1 and r.operador.id_usuario = ?2 and r.dataFim is null")
    List<RegistroOperacao> buscarOperacoesAbertasPorMaquinasEUsuario(List<Long> maquinaIds, Long usuarioId);

    @Query("select r from RegistroOperacao r where r.operador.id_usuario = ?1 and r.dataFim is null order by r.dataInicio desc")
    List<RegistroOperacao> buscarOperacaoAbertaPorUsuario(Long usuarioId);
}
