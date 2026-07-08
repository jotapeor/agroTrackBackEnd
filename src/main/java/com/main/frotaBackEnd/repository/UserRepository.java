package com.main.frotaBackEnd.repository;

import com.main.frotaBackEnd.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends JpaRepository<Usuario, Long> {
    @Query("select u from Usuario u where u.email = ?1 and u.senha = ?2")
    Usuario login(String email, String senha);

    @Query("select count(u) > 0 from Usuario u where u.email = ?1")
    boolean emailExiste(String email);

    @Modifying
    @Transactional
    @Query("update Usuario u set u.senha = ?1, u.primeiro_acesso = false where u.id_usuario = ?2")
    int alterarSenha(String novaSenha, Long idUsuario);
}
