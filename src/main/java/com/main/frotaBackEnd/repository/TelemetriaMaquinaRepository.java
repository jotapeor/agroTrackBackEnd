package com.main.frotaBackEnd.repository;

import com.main.frotaBackEnd.model.TelemetriaMaquina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TelemetriaMaquinaRepository extends JpaRepository<TelemetriaMaquina, Long> {
    Optional<TelemetriaMaquina> findTopByMaquinaIdOrderByDataAtualizacaoDesc(Long maquinaId);
}
