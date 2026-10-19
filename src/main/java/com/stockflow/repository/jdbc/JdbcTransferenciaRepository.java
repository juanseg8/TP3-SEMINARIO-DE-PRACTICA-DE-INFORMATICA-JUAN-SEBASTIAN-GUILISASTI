package com.stockflow.repository.jdbc;

import com.stockflow.config.ConnectionFactory;
import com.stockflow.exception.ConexionBaseDatosException;
import com.stockflow.exception.TransferenciaException;
import com.stockflow.model.MovimientoEntrada;
import com.stockflow.model.MovimientoInventario;
import com.stockflow.model.MovimientoSalida;
import com.stockflow.model.Transferencia;
import com.stockflow.repository.TransferenciaRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class JdbcTransferenciaRepository implements TransferenciaRepository {
    @Override
    public List<Transferencia> listar() {
        String sql = """
                SELECT t.id_transferencia, t.id_recomendacion, t.cantidad, t.estado, t.fecha_registro,
                       io.id_inventario AS id_origen, id.id_inventario AS id_destino,
                       p.nombre AS producto, so.nombre AS origen, sd.nombre AS destino, u.nombre AS responsable
                FROM transferencia t
                JOIN recomendacion r ON r.id_recomendacion = t.id_recomendacion
                JOIN inventario io ON io.id_inventario = r.id_inventario_origen
                JOIN inventario id ON id.id_inventario = r.id_inventario_destino
                JOIN producto p ON p.id_producto = io.id_producto
                JOIN sucursal so ON so.id_sucursal = io.id_sucursal
                JOIN sucursal sd ON sd.id_sucursal = id.id_sucursal
                JOIN usuario u ON u.id_usuario = t.id_usuario_responsable
                ORDER BY CASE t.estado WHEN 'EN_TRANSITO' THEN 0 WHEN 'PENDIENTE' THEN 1 ELSE 2 END,
                         t.fecha_registro DESC
                """;
        List<Transferencia> transferencias = new ArrayList<>();
        try (Connection connection = ConnectionFactory.abrir();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Timestamp fecha = result.getTimestamp("fecha_registro");
                transferencias.add(new Transferencia(
                        result.getInt("id_transferencia"), result.getInt("id_recomendacion"),
                        result.getInt("id_origen"), result.getInt("id_destino"),
                        result.getString("producto"), result.getString("origen"), result.getString("destino"),
                        result.getInt("cantidad"), result.getString("estado"),
                        fecha == null ? null : fecha.toLocalDateTime(), result.getString("responsable")));
            }
            return transferencias;
        } catch (SQLException e) {
            throw new ConexionBaseDatosException("No se pudieron consultar las transferencias.", e);
        }
    }

    @Override
    public void iniciar(int transferenciaId, int usuarioId) {
        cambiarEstado(transferenciaId, usuarioId, "PENDIENTE", "EN_TRANSITO", "Traslado iniciado.");
    }

    @Override
    public void cancelar(int transferenciaId, int usuarioId, String motivo) {
        if (motivo == null || motivo.isBlank()) throw new TransferenciaException("Indique el motivo de la cancelación.");
        String update = """
                UPDATE transferencia SET estado='CANCELADA', observacion=?
                WHERE id_transferencia=? AND estado IN ('PENDIENTE','EN_TRANSITO')
                """;
        ejecutarCambio(transferenciaId, usuarioId, update, motivo.trim(), "CANCELADA", motivo.trim());
    }

    @Override
    public void completar(int transferenciaId, int usuarioId) {
        String select = """
                SELECT t.estado, t.cantidad, r.id_inventario_origen, r.id_inventario_destino,
                       io.id_producto AS producto_origen, id.id_producto AS producto_destino,
                       io.stock_actual AS stock_origen, id.stock_actual AS stock_destino
                FROM transferencia t
                JOIN recomendacion r ON r.id_recomendacion = t.id_recomendacion
                JOIN inventario io ON io.id_inventario = r.id_inventario_origen
                JOIN inventario id ON id.id_inventario = r.id_inventario_destino
                WHERE t.id_transferencia=?
                FOR UPDATE
                """;
        String updateStock = "UPDATE inventario SET stock_actual=? WHERE id_inventario=?";
        String insertMovimiento = """
                INSERT INTO movimiento (id_inventario, id_usuario, id_transferencia, tipo, cantidad, observacion)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        String updateTransferencia = """
                UPDATE transferencia SET estado='COMPLETADA', fecha_finalizacion=NOW()
                WHERE id_transferencia=? AND estado='EN_TRANSITO'
                """;
        try (Connection connection = ConnectionFactory.abrir()) {
            connection.setAutoCommit(false);
            try {
                DatosTransferencia datos = bloquear(connection, select, transferenciaId);
                if (!"EN_TRANSITO".equals(datos.estado())) {
                    throw new TransferenciaException("Solo puede completarse una transferencia en tránsito.");
                }
                if (datos.productoOrigen() != datos.productoDestino()) {
                    throw new TransferenciaException("Origen y destino no corresponden al mismo producto.");
                }

                List<MovimientoInventario> movimientos = List.of(
                        new MovimientoSalida(datos.inventarioOrigen(), datos.cantidad()),
                        new MovimientoEntrada(datos.inventarioDestino(), datos.cantidad()));
                int[] stocks = {datos.stockOrigen(), datos.stockDestino()};

                for (int i = 0; i < movimientos.size(); i++) {
                    MovimientoInventario movimiento = movimientos.get(i);
                    int nuevoStock;
                    try {
                        nuevoStock = movimiento.aplicarSobre(stocks[i]);
                    } catch (IllegalStateException e) {
                        throw new TransferenciaException(e.getMessage());
                    }
                    try (PreparedStatement statement = connection.prepareStatement(updateStock)) {
                        statement.setInt(1, nuevoStock);
                        statement.setInt(2, movimiento.getInventarioId());
                        statement.executeUpdate();
                    }
                    try (PreparedStatement statement = connection.prepareStatement(insertMovimiento)) {
                        statement.setInt(1, movimiento.getInventarioId());
                        statement.setInt(2, usuarioId);
                        statement.setInt(3, transferenciaId);
                        statement.setString(4, movimiento.getTipo());
                        statement.setInt(5, movimiento.getCantidad());
                        statement.setString(6, movimiento.getTipo().equals("SALIDA")
                                ? "Salida por transferencia interna" : "Entrada por transferencia interna");
                        statement.executeUpdate();
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(updateTransferencia)) {
                    statement.setInt(1, transferenciaId);
                    if (statement.executeUpdate() != 1) throw new TransferenciaException("La transferencia cambió de estado.");
                }
                insertarHistorial(connection, transferenciaId, usuarioId, "COMPLETADA",
                        "Transferencia finalizada. Inventarios actualizados atómicamente.");
                connection.commit();
            } catch (RuntimeException | SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (TransferenciaException e) {
            throw e;
        } catch (SQLException e) {
            throw new TransferenciaException("No se pudo completar la transferencia; no se aplicó ningún cambio.", e);
        }
    }

    private void cambiarEstado(int transferenciaId, int usuarioId, String estadoActual,
                               String estadoNuevo, String observacion) {
        String update = "UPDATE transferencia SET estado=? WHERE id_transferencia=? AND estado=?";
        try (Connection connection = ConnectionFactory.abrir()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(update)) {
                statement.setString(1, estadoNuevo);
                statement.setInt(2, transferenciaId);
                statement.setString(3, estadoActual);
                if (statement.executeUpdate() != 1) throw new TransferenciaException("La transferencia ya cambió de estado.");
                insertarHistorial(connection, transferenciaId, usuarioId, estadoNuevo, observacion);
                connection.commit();
            } catch (RuntimeException | SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (TransferenciaException e) {
            throw e;
        } catch (SQLException e) {
            throw new ConexionBaseDatosException("No se pudo actualizar la transferencia.", e);
        }
    }

    private void ejecutarCambio(int transferenciaId, int usuarioId, String update, String parametro,
                                String estadoNuevo, String observacion) {
        try (Connection connection = ConnectionFactory.abrir()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(update)) {
                statement.setString(1, parametro);
                statement.setInt(2, transferenciaId);
                if (statement.executeUpdate() != 1) throw new TransferenciaException("La transferencia no puede cancelarse.");
                insertarHistorial(connection, transferenciaId, usuarioId, estadoNuevo, observacion);
                connection.commit();
            } catch (RuntimeException | SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (TransferenciaException e) {
            throw e;
        } catch (SQLException e) {
            throw new ConexionBaseDatosException("No se pudo cancelar la transferencia.", e);
        }
    }

    private DatosTransferencia bloquear(Connection connection, String sql, int transferenciaId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, transferenciaId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new TransferenciaException("No se encontró la transferencia.");
                return new DatosTransferencia(result.getString("estado"), result.getInt("cantidad"),
                        result.getInt("id_inventario_origen"), result.getInt("id_inventario_destino"),
                        result.getInt("producto_origen"), result.getInt("producto_destino"),
                        result.getInt("stock_origen"), result.getInt("stock_destino"));
            }
        }
    }

    private void insertarHistorial(Connection connection, int transferenciaId, int usuarioId,
                                   String estado, String observacion) throws SQLException {
        String sql = """
                INSERT INTO historial_transferencia (id_transferencia, id_usuario, estado, observacion)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, transferenciaId);
            statement.setInt(2, usuarioId);
            statement.setString(3, estado);
            statement.setString(4, observacion);
            statement.executeUpdate();
        }
    }

    private record DatosTransferencia(String estado, int cantidad, int inventarioOrigen, int inventarioDestino,
                                      int productoOrigen, int productoDestino, int stockOrigen, int stockDestino) { }
}
