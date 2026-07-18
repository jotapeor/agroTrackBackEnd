package com.main.frotaBackEnd.repository;

import com.main.frotaBackEnd.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    @Query("select n from Notificacao n where n.usuarioDestinatario.id_usuario = ?1 order by n.dataCriacao desc")
    List<Notificacao> buscarPorUsuarioId(Long usuarioId);

    @Query("select n from Notificacao n where n.maquina.id = ?1 order by n.dataCriacao desc")
    List<Notificacao> buscarPorMaquinaId(Long maquinaId);

    @Query("select n from Notificacao n where n.maquina.id = ?1 and n.dataCriacao >= ?2 and n.dataCriacao <= ?3 order by n.dataCriacao desc")
    List<Notificacao> buscarPorMaquinaEIntervalo(Long maquinaId, java.time.LocalDateTime inicio, java.time.LocalDateTime fim);
}
