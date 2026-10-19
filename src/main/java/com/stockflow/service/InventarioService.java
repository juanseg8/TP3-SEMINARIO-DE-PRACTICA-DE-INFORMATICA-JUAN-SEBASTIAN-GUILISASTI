package com.stockflow.service;

import com.stockflow.model.FiltroInventario;
import com.stockflow.model.Inventario;
import com.stockflow.model.Producto;
import com.stockflow.model.Sucursal;
import com.stockflow.repository.InventarioRepository;
import com.stockflow.repository.jdbc.JdbcInventarioRepository;

import java.util.List;

public class InventarioService {
    private final InventarioRepository repository;

    public InventarioService() { this(new JdbcInventarioRepository()); }
    public InventarioService(InventarioRepository repository) { this.repository = repository; }

    public List<Inventario> buscar(String producto, String sucursal, String estado) {
        return repository.buscar(new FiltroInventario(producto, sucursal, estado));
    }

    public List<Producto> listarProductos() { return repository.listarProductos(); }
    public List<Sucursal> listarSucursales() { return repository.listarSucursales(); }
}
