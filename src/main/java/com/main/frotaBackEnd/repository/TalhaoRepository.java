package com.main.frotaBackEnd.repository;

import com.main.frotaBackEnd.model.Talhao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TalhaoRepository extends JpaRepository<Talhao, Long> {
    @Query("select t from Talhao t where t.fazenda.id = ?1 and t.ativo = true order by t.nome")
    List<Talhao> buscarPorFazenda(Long idFazenda);
}
