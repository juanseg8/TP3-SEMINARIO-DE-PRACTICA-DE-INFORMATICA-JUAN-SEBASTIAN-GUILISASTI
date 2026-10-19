package com.stockflow.repository.jdbc;

import com.stockflow.config.ConnectionFactory;
import com.stockflow.exception.ConexionBaseDatosException;
import com.stockflow.exception.ValidacionException;
import com.stockflow.model.Inventario;
import com.stockflow.model.Producto;
import com.stockflow.model.Recomendacion;
import com.stockflow.model.Sucursal;
import com.stockflow.repository.RecomendacionRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class JdbcRecomendacionRepository implements RecomendacionRepository {
    private static final String SELECT_BASE = """
            SELECT r.id_recomendacion, r.cantidad_recomendada, r.cantidad_aprobada, r.estado, r.fecha_generacion,
                   p.id_producto, p.codigo, p.nombre AS producto,
                   io.id_inventario AS id_origen, io.stock_actual AS stock_origen,
                   io.stock_minimo AS minimo_origen, io.stock_maximo AS maximo_origen,
                   so.id_sucursal AS id_sucursal_origen, so.nombre AS origen,
                   id.id_inventario AS id_destino, id.stock_actual AS stock_destino,
                   id.stock_minimo AS minimo_destino, id.stock_maximo AS maximo_destino,
                   sd.id_sucursal AS id_sucursal_destino, sd.nombre AS destino
            FROM recomendacion r
            JOIN inventario io ON io.id_inventario = r.id_inventario_origen
            JOIN inventario id ON id.id_inventario = r.id_inventario_destino
            JOIN producto p ON p.id_producto = io.id_producto
            JOIN sucursal so ON so.id_sucursal = io.id_sucursal
            JOIN sucursal sd ON sd.id_sucursal = id.id_sucursal
            ORDER BY CASE r.estado WHEN 'PENDIENTE' THEN 0 ELSE 1 END, r.fecha_generacion DESC
            """;

    @Override
    public List<Recomendacion> listar() {
        List<Recomendacion> recomendaciones = new ArrayList<>();
        try (Connection connection = ConnectionFactory.abrir();
             PreparedStatement statement = connection.prepareStatement(SELECT_BASE);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) recomendaciones.add(mapear(result));
            return recomendaciones;
        } catch (SQLException e) {
            throw new ConexionBaseDatosException("No se pudieron consultar las recomendaciones.", e);
        }
    }

    @Override
    public int generarPendientes() {
        String sql = """
                INSERT INTO recomendacion (id_inventario_origen, id_inventario_destino, cantidad_recomendada, estado)
                SELECT io.id_inventario, id.id_inventario,
                       LEAST(io.stock_actual - io.stock_maximo, id.stock_minimo - id.stock_actual), 'PENDIENTE'
                FROM inventario io
                JOIN inventario id ON id.id_producto = io.id_producto AND id.id_sucursal <> io.id_sucursal
                WHERE io.stock_actual > io.stock_maximo
                  AND id.stock_actual < id.stock_minimo
                  AND NOT EXISTS (
                      SELECT 1 FROM recomendacion r
                      WHERE r.id_inventario_origen = io.id_inventario
                        AND r.id_inventario_destino = id.id_inventario
                        AND r.estado = 'PENDIENTE'
                  )
                """;
        try (Connection connection = ConnectionFactory.abrir();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new ConexionBaseDatosException("No se pudieron generar las recomendaciones.", e);
        }
    }

    @Override
    public void aprobar(int recomendacionId, int cantidad, int usuarioId, String motivo) {
        if (cantidad <= 0) throw new ValidacionException("La cantidad aprobada debe ser mayor que cero.");
        String update = """
                UPDATE recomendacion
                SET estado='APROBADA', cantidad_aprobada=?, id_usuario_decision=?, fecha_decision=NOW(), motivo_decision=?
                WHERE id_recomendacion=? AND estado='PENDIENTE' AND ? <= cantidad_recomendada
                """;
        String insertTransferencia = """
                INSERT INTO transferencia (id_recomendacion, id_usuario_responsable, cantidad, estado, observacion)
                VALUES (?, ?, ?, 'PENDIENTE', 'Creada al aprobar la recomendación')
                """;
        String insertHistorial = """
                INSERT INTO historial_transferencia (id_transferencia, id_usuario, estado, observacion)
                VALUES (?, ?, 'PENDIENTE', 'Transferencia registrada.')
                """;
        try (Connection connection = ConnectionFactory.abrir()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(update)) {
                    statement.setInt(1, cantidad);
                    statement.setInt(2, usuarioId);
                    statement.setString(3, motivo);
                    statement.setInt(4, recomendacionId);
                    statement.setInt(5, cantidad);
                    if (statement.executeUpdate() != 1) {
                        throw new ValidacionException("La recomendación ya fue procesada o la cantidad no es válida.");
                    }
                }
                int transferenciaId;
                try (PreparedStatement statement = connection.prepareStatement(insertTransferencia, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, recomendacionId);
                    statement.setInt(2, usuarioId);
                    statement.setInt(3, cantidad);
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("No se obtuvo el identificador de la transferencia");
                        transferenciaId = keys.getInt(1);
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(insertHistorial)) {
                    statement.setInt(1, transferenciaId);
                    statement.setInt(2, usuarioId);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (RuntimeException | SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (ValidacionException e) {
            throw e;
        } catch (SQLException e) {
            throw new ConexionBaseDatosException("No se pudo aprobar la recomendación.", e);
        }
    }

    @Override
    public void rechazar(int recomendacionId, int usuarioId, String motivo) {
        if (motivo == null || motivo.isBlank()) throw new ValidacionException("Indique el motivo del rechazo.");
        String sql = """
                UPDATE recomendacion
                SET estado='RECHAZADA', id_usuario_decision=?, fecha_decision=NOW(), motivo_decision=?
                WHERE id_recomendacion=? AND estado='PENDIENTE'
                """;
        try (Connection connection = ConnectionFactory.abrir();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, usuarioId);
            statement.setString(2, motivo.trim());
            statement.setInt(3, recomendacionId);
            if (statement.executeUpdate() != 1) throw new ValidacionException("La recomendación ya fue procesada.");
        } catch (ValidacionException e) {
            throw e;
        } catch (SQLException e) {
            throw new ConexionBaseDatosException("No se pudo rechazar la recomendación.", e);
        }
    }

    private Recomendacion mapear(ResultSet result) throws SQLException {
        Producto producto = new Producto(result.getInt("id_producto"), result.getString("codigo"), result.getString("producto"));
        Inventario origen = new Inventario(result.getInt("id_origen"), producto,
                new Sucursal(result.getInt("id_sucursal_origen"), result.getString("origen")),
                result.getInt("stock_origen"), result.getInt("minimo_origen"), result.getInt("maximo_origen"));
        Inventario destino = new Inventario(result.getInt("id_destino"), producto,
                new Sucursal(result.getInt("id_sucursal_destino"), result.getString("destino")),
                result.getInt("stock_destino"), result.getInt("minimo_destino"), result.getInt("maximo_destino"));
        Integer aprobada = result.getObject("cantidad_aprobada", Integer.class);
        Timestamp fecha = result.getTimestamp("fecha_generacion");
        return new Recomendacion(result.getInt("id_recomendacion"), producto, origen, destino,
                result.getInt("cantidad_recomendada"), aprobada, result.getString("estado"),
                fecha == null ? null : fecha.toLocalDateTime());
    }
}
