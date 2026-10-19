package com.stockflow.model;

public abstract class MovimientoInventario {
    private final int inventarioId;
    private final int cantidad;
    private final String tipo;

    protected MovimientoInventario(int inventarioId, int cantidad, String tipo) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        this.inventarioId = inventarioId;
        this.cantidad = cantidad;
        this.tipo = tipo;
    }

    public int getInventarioId() { return inventarioId; }
    public int getCantidad() { return cantidad; }
    public String getTipo() { return tipo; }
    public abstract int aplicarSobre(int stockActual);
}
