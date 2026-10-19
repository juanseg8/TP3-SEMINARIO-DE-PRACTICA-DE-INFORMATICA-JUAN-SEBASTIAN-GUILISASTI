package com.stockflow.model;
public class Inventario {
    private final int id;
    private final Producto producto;
    private final Sucursal sucursal;
    private final int stockActual;
    private final int stockMinimo;
    private final int stockMaximo;

    public Inventario(Producto producto, Sucursal sucursal, int stockActual, int stockMinimo, int stockMaximo) {
        this(0, producto, sucursal, stockActual, stockMinimo, stockMaximo);
    }

    public Inventario(int id, Producto producto, Sucursal sucursal, int stockActual, int stockMinimo, int stockMaximo) {
        if (producto == null || sucursal == null) {
            throw new IllegalArgumentException("Producto y sucursal son obligatorios");
        }
        if (stockActual < 0 || stockMinimo < 0 || stockMaximo < stockMinimo) {
            throw new IllegalArgumentException("Los niveles de stock no son válidos");
        }
        this.id = id;
        this.producto = producto;
        this.sucursal = sucursal;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.stockMaximo = stockMaximo;
    }

    public int getId() { return id; }
    public Producto getProducto() { return producto; }
    public Sucursal getSucursal() { return sucursal; }
    public String getProductoNombre() { return producto.getNombre(); }
    public String getSucursalNombre() { return sucursal.getNombre(); }
    public int getStockActual() { return stockActual; }
    public int getStockMinimo() { return stockMinimo; }
    public int getStockMaximo() { return stockMaximo; }

    public String getEstado() {
        if (stockActual < stockMinimo) return "Déficit";
        if (stockActual > stockMaximo) return "Sobrestock";
        return "Normal";
    }
}
