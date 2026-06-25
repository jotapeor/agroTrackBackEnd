package com.main.frotaBackEnd.repository;

import com.main.frotaBackEnd.model.Conexao;
import com.main.frotaBackEnd.model.UserDTO;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class UserRepository {

    public UserDTO login(String email, String senha) {
        UserDTO user = new UserDTO(); // Objeto vazio; só será populado se as credenciais forem válidas
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement(
                    "select * from usuario where email = ? and senha = ?"
            );
            stmt.setString(1, email);
            stmt.setString(2, senha);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) { // rs.next() retorna true apenas se email + senha correspondem a um registro
                user.setId_usuario(rs.getLong("id_usuario"));
                user.setNome(rs.getString("nome"));
                user.setEmail(rs.getString("email"));
                user.setSenha(rs.getString("senha"));
                user.setPerfil(rs.getString("perfil"));
                user.setAtivo(rs.getBoolean("ativo"));
                user.setData_criacao(rs.getDate("data_criacao"));
            }
            // Se rs.next() retornou false, o objeto "user" permanece sem id — o UserService lança 401
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }
}