package com.stockflow.model;

public class MovimientoEntrada extends MovimientoInventario {
    public MovimientoEntrada(int inventarioId, int cantidad) {
        super(inventarioId, cantidad, "ENTRADA");
    }

    @Override
    public int aplicarSobre(int stockActual) {
        return stockActual + getCantidad();
    }
}
