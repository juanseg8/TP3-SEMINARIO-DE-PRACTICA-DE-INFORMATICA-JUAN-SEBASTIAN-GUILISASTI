package com.stockflow.repository.jdbc;

import com.stockflow.config.ConnectionFactory;
import com.stockflow.exception.ConexionBaseDatosException;
import com.stockflow.model.FiltroInventario;
import com.stockflow.model.Inventario;
import com.stockflow.model.Producto;
import com.stockflow.model.Sucursal;
import com.stockflow.repository.InventarioRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcInventarioRepository implements InventarioRepository {
    @Override
    public List<Inventario> buscar(FiltroInventario filtro) {
        StringBuilder sql = new StringBuilder("""
                SELECT i.id_inventario, i.stock_actual, i.stock_minimo, i.stock_maximo,
                       p.id_producto, p.codigo, p.nombre AS producto,
                       s.id_sucursal, s.nombre AS sucursal
                FROM inventario i
                JOIN producto p ON p.id_producto = i.id_producto
                JOIN sucursal s ON s.id_sucursal = i.id_sucursal
                WHERE p.activo = TRUE AND s.activo = TRUE
                """);
        List<String> parametros = new ArrayList<>();
        if (filtro.filtraProducto()) {
            sql.append(" AND p.nombre = ?");
            parametros.add(filtro.getProducto());
        }
        if (filtro.filtraSucursal()) {
            sql.append(" AND s.nombre = ?");
            parametros.add(filtro.getSucursal());
        }
        if (filtro.filtraEstado()) {
            switch (filtro.getEstado()) {
                case "Déficit" -> sql.append(" AND i.stock_actual < i.stock_minimo");
                case "Sobrestock" -> sql.append(" AND i.stock_actual > i.stock_maximo");
                case "Normal" -> sql.append(" AND i.stock_actual BETWEEN i.stock_minimo AND i.stock_maximo");
                default -> { }
            }
        }
        sql.append(" ORDER BY p.nombre, s.nombre");

        List<Inventario> inventarios = new ArrayList<>();
        try (Connection connection = ConnectionFactory.abrir();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) statement.setString(i + 1, parametros.get(i));
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) inventarios.add(mapear(result));
            }
            return inventarios;
        } catch (SQLException e) {
            throw new ConexionBaseDatosException("No se pudo consultar el inventario.", e);
        }
    }

    @Override
    public List<Producto> listarProductos() {
        String sql = "SELECT id_producto, codigo, nombre FROM producto WHERE activo = TRUE ORDER BY nombre";
        List<Producto> productos = new ArrayList<>();
        try (Connection connection = ConnectionFactory.abrir();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) productos.add(new Producto(
                    result.getInt("id_producto"), result.getString("codigo"), result.getString("nombre")));
            return productos;
        } catch (SQLException e) {
            throw new ConexionBaseDatosException("No se pudieron consultar los productos.", e);
        }
    }

    @Override
    public List<Sucursal> listarSucursales() {
        String sql = "SELECT id_sucursal, nombre FROM sucursal WHERE activo = TRUE ORDER BY nombre";
        List<Sucursal> sucursales = new ArrayList<>();
        try (Connection connection = ConnectionFactory.abrir();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) sucursales.add(new Sucursal(result.getInt("id_sucursal"), result.getString("nombre")));
            return sucursales;
        } catch (SQLException e) {
            throw new ConexionBaseDatosException("No se pudieron consultar las sucursales.", e);
        }
    }

    private Inventario mapear(ResultSet result) throws SQLException {
        Producto producto = new Producto(result.getInt("id_producto"), result.getString("codigo"), result.getString("producto"));
        Sucursal sucursal = new Sucursal(result.getInt("id_sucursal"), result.getString("sucursal"));
        return new Inventario(result.getInt("id_inventario"), producto, sucursal,
                result.getInt("stock_actual"), result.getInt("stock_minimo"), result.getInt("stock_maximo"));
    }
}
