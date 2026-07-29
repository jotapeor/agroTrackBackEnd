package com.main.frotaBackEnd.repository;

import com.main.frotaBackEnd.model.HistoricoStatusMaquina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoStatusMaquinaRepository extends JpaRepository<HistoricoStatusMaquina, Long> {

    @Query("select h from HistoricoStatusMaquina h where h.maquina.id = ?1 order by h.dataAlteracao desc")
    List<HistoricoStatusMaquina> buscarPorMaquinaId(Long maquinaId);
}
