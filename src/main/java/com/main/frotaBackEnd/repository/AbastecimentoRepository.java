package com.main.frotaBackEnd.repository;

import com.main.frotaBackEnd.model.Abastecimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AbastecimentoRepository extends JpaRepository<Abastecimento, Long> {

    @Query("select a from Abastecimento a where a.maquina.id = ?1 order by a.dataAbastecimento desc")
    List<Abastecimento> buscarPorMaquinaId(Long maquinaId);

    @Query("select a from Abastecimento a where a.operador.id_usuario = ?1 order by a.dataAbastecimento desc")
    List<Abastecimento> buscarPorOperadorId(Long operadorId);

    @Query("select a from Abastecimento a where a.maquina.id = ?1 and a.dataAbastecimento >= ?2 and a.dataAbastecimento <= ?3 order by a.dataAbastecimento desc")
    List<Abastecimento> buscarPorMaquinaEIntervalo(Long maquinaId, java.time.LocalDateTime inicio, java.time.LocalDateTime fim);
}
