package com.main.frotaBackEnd.repository;

import com.main.frotaBackEnd.model.OrdemManutencao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdemManutencaoRepository extends JpaRepository<OrdemManutencao, Long> {

    @Query("select o from OrdemManutencao o where o.maquina.id = ?1 order by o.dataAbertura desc")
    List<OrdemManutencao> buscarPorMaquinaId(Long maquinaId);

    @Query("select o from OrdemManutencao o where o.abertaPor.id_usuario = ?1 order by o.dataAbertura desc")
    List<OrdemManutencao> buscarPorSolicitanteId(Long solicitanteId);
}
