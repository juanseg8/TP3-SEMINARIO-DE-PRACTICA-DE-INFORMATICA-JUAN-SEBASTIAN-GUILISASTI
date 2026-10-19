package com.stockflow.repository;

import com.stockflow.model.FiltroInventario;
import com.stockflow.model.Inventario;
import com.stockflow.model.Producto;
import com.stockflow.model.Sucursal;

import java.util.List;

public interface InventarioRepository {
    List<Inventario> buscar(FiltroInventario filtro);
    List<Producto> listarProductos();
    List<Sucursal> listarSucursales();
}
