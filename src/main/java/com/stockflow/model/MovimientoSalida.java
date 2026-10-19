package com.stockflow.model;

public class MovimientoSalida extends MovimientoInventario {
    public MovimientoSalida(int inventarioId, int cantidad) {
        super(inventarioId, cantidad, "SALIDA");
    }

    @Override
    public int aplicarSobre(int stockActual) {
        if (stockActual < getCantidad()) {
            throw new IllegalStateException("Stock insuficiente para completar la transferencia");
        }
        return stockActual - getCantidad();
    }
}
