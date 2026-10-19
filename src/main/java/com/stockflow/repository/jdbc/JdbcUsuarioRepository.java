package com.stockflow.repository.jdbc;

import com.stockflow.config.ConnectionFactory;
import com.stockflow.exception.ConexionBaseDatosException;
import com.stockflow.model.Usuario;
import com.stockflow.repository.UsuarioRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class JdbcUsuarioRepository implements UsuarioRepository {
    private static final String SQL = """
            SELECT u.id_usuario, u.nombre, u.usuario, u.clave_hash, r.nombre AS rol
            FROM usuario u
            JOIN rol r ON r.id_rol = u.id_rol
            WHERE u.usuario = ? AND u.activo = TRUE
            """;

    @Override
    public Optional<Usuario> buscarActivoPorUsuario(String usuario) {
        try (Connection connection = ConnectionFactory.abrir();
             PreparedStatement statement = connection.prepareStatement(SQL)) {
            statement.setString(1, usuario);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return Optional.empty();
                return Optional.of(new Usuario(
                        result.getInt("id_usuario"), result.getString("nombre"),
                        result.getString("usuario"), result.getString("clave_hash"), result.getString("rol")));
            }
        } catch (SQLException e) {
            throw new ConexionBaseDatosException("No se pudo consultar el usuario.", e);
        }
    }
}
