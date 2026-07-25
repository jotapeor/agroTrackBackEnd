package com.main.frotaBackEnd.repository;

import com.main.frotaBackEnd.model.Maquina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaquinaRepository extends JpaRepository<Maquina, Long> {
    @Query("select m from Maquina m where m.ativo = true order by m.dataCadastro desc")
    List<Maquina> buscarTodosOrdenados();

    @Query("select m from Maquina m join m.usuarios u where u.id_usuario = ?1 and m.ativo = true order by m.dataCadastro desc")
    List<Maquina> buscarPorUsuarioId(Long usuarioId);

    @Query("select m from Maquina m where m.ativo = true and m.status = 'Em Operacao' order by m.nome asc")
    List<Maquina> buscarEmOperacao();

    @Query("select m from Maquina m where m.ativo = false order by m.dataCadastro desc")
    List<Maquina> buscarArquivadas();
}
