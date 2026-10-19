package com.stockflow.config;

import com.stockflow.exception.ConexionBaseDatosException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionFactory {
    private ConnectionFactory() { }

    public static Connection abrir() {
        try {
            return DriverManager.getConnection(
                    DatabaseConfig.getUrl(), DatabaseConfig.getUsuario(), DatabaseConfig.getPassword());
        } catch (SQLException e) {
            throw new ConexionBaseDatosException(
                    "No se pudo conectar con MySQL. Verifique que el servicio MySQL84 esté iniciado.", e);
        }
    }
}
