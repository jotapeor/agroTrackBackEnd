package com.main.frotaBackEnd.repository;

import com.main.frotaBackEnd.model.Fazenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FazendaRepository extends JpaRepository<Fazenda, Long> {
    List<Fazenda> findAllByAtivoTrueOrderByNome();
}
