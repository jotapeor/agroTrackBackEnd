package com.main.frotaBackEnd.repository;

import com.main.frotaBackEnd.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends JpaRepository<Usuario, Long> {
    @Query("select u from Usuario u where u.email = ?1")
    Usuario login(String email);

    @Query("select count(u) > 0 from Usuario u where u.email = ?1")
    boolean emailExiste(String email);

    Usuario findByEmail(String email);

    @Modifying
    @Transactional
    @Query("update Usuario u set u.senha = ?1, u.primeiro_acesso = false where u.id_usuario = ?2")
    int alterarSenha(String novaSenha, Long idUsuario);

    @Query("select count(m) > 0 from Usuario u join u.maquinas m where u.id_usuario = ?1 and m.id = ?2")
    boolean verificaVinculo(Long usuarioId, Long maquinaId);
}
