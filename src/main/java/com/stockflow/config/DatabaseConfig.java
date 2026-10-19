package com.stockflow.config;

import com.stockflow.exception.ConexionBaseDatosException;

public final class DatabaseConfig {
    private static final String URL_DEFAULT =
            "jdbc:mysql://localhost:3306/stockflow_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    private DatabaseConfig() { }

    public static String getUrl() {
        return obtener("STOCKFLOW_DB_URL", "stockflow.db.url", URL_DEFAULT, false);
    }

    public static String getUsuario() {
        return obtener("STOCKFLOW_DB_USER", "stockflow.db.user", "stockflow_app", false);
    }

    public static String getPassword() {
        return obtener("STOCKFLOW_DB_PASSWORD", "stockflow.db.password", null, true);
    }

    private static String obtener(String variable, String propiedad, String valorDefault, boolean obligatorio) {
        String valor = System.getenv(variable);
        if (valor == null || valor.isBlank()) {
            valor = System.getProperty(propiedad);
        }
        if (valor == null || valor.isBlank()) {
            valor = valorDefault;
        }
        if (obligatorio && (valor == null || valor.isBlank())) {
            throw new ConexionBaseDatosException(
                    "Falta configurar la variable " + variable + ". Reinicie la terminal si acaba de crearla.", null);
        }
        return valor;
    }
}
