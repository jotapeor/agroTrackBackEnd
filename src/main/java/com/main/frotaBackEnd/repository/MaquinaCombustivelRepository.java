package com.main.frotaBackEnd.repository;

import com.main.frotaBackEnd.model.MaquinaCombustivel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaquinaCombustivelRepository extends JpaRepository<MaquinaCombustivel, Long> {

    @Query("select c.tipoCombustivel from MaquinaCombustivel c where c.maquina.id = ?1")
    List<String> findTiposByMaquinaId(Long idMaquina);

    void deleteByMaquina_Id(Long idMaquina);
}
