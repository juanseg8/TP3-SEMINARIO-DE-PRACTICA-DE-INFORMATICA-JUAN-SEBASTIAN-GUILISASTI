package com.stockflow.repository.jdbc;

import com.stockflow.config.ConnectionFactory;
import com.stockflow.exception.ConexionBaseDatosException;
import com.stockflow.model.ResumenInicio;
import com.stockflow.repository.ResumenRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcResumenRepository implements ResumenRepository {
    private static final String SQL = """
            SELECT
                (SELECT COUNT(*) FROM inventario WHERE stock_actual < stock_minimo) AS deficit,
                (SELECT COUNT(*) FROM inventario WHERE stock_actual > stock_maximo) AS sobrestock,
                (SELECT COUNT(*) FROM recomendacion WHERE estado = 'PENDIENTE') AS recomendaciones_pendientes,
                (SELECT COUNT(*) FROM transferencia WHERE estado = 'EN_TRANSITO') AS transferencias_en_transito
            """;

    @Override
    public ResumenInicio obtenerResumen() {
        try (Connection connection = ConnectionFactory.abrir();
             PreparedStatement statement = connection.prepareStatement(SQL);
             ResultSet result = statement.executeQuery()) {
            if (!result.next()) {
                throw new ConexionBaseDatosException("MySQL no devolvió el resumen del sistema.", null);
            }
            return new ResumenInicio(
                    result.getInt("deficit"),
                    result.getInt("sobrestock"),
                    result.getInt("recomendaciones_pendientes"),
                    result.getInt("transferencias_en_transito"));
        } catch (SQLException e) {
            throw new ConexionBaseDatosException("No se pudo consultar el resumen del sistema.", e);
        }
    }
}
