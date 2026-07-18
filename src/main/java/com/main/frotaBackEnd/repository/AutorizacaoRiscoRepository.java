package com.main.frotaBackEnd.repository;

import com.main.frotaBackEnd.model.AutorizacaoRisco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutorizacaoRiscoRepository extends JpaRepository<AutorizacaoRisco, Long> {

    @Query("select a from AutorizacaoRisco a where a.maquina.id = ?1 order by a.dataAutorizacao desc")
    List<AutorizacaoRisco> buscarPorMaquinaId(Long maquinaId);
}
