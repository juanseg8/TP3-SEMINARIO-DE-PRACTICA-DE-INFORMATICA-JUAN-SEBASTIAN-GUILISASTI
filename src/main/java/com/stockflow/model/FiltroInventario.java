package com.stockflow.model;

public class FiltroInventario {
    private final String producto;
    private final String sucursal;
    private final String estado;

    public FiltroInventario(String producto, String sucursal, String estado) {
        this.producto = producto;
        this.sucursal = sucursal;
        this.estado = estado;
    }

    public String getProducto() { return producto; }
    public String getSucursal() { return sucursal; }
    public String getEstado() { return estado; }
    public boolean filtraProducto() { return producto != null && !"Todos".equals(producto); }
    public boolean filtraSucursal() { return sucursal != null && !"Todos".equals(sucursal); }
    public boolean filtraEstado() { return estado != null && !"Todos".equals(estado); }
}
