package com.stockflow.model;

import java.time.LocalDateTime;

public class Recomendacion {
    private final int id;
    private final Producto producto;
    private final Inventario invOrigen;
    private final Inventario invDestino;
    private final int cantidad;
    private final Integer cantidadAprobada;
    private final String estado;
    private final LocalDateTime fechaGeneracion;

    public Recomendacion(Producto producto, Inventario invOrigen, Inventario invDestino, int cantidad, String estado) {
        this(0, producto, invOrigen, invDestino, cantidad, null, estado, null);
    }

    public Recomendacion(int id, Producto producto, Inventario invOrigen, Inventario invDestino,
                         int cantidad, Integer cantidadAprobada, String estado, LocalDateTime fechaGeneracion) {
        if (producto == null || invOrigen == null || invDestino == null || cantidad <= 0) {
            throw new IllegalArgumentException("Los datos de la recomendación no son válidos");
        }
        this.id = id;
        this.producto = producto;
        this.invOrigen = invOrigen;
        this.invDestino = invDestino;
        this.cantidad = cantidad;
        this.cantidadAprobada = cantidadAprobada;
        this.estado = estado;
        this.fechaGeneracion = fechaGeneracion;
    }

    public int getId() { return id; }
    public String getProductoNombre() { return producto.getNombre(); }
    public String getOrigen() { return invOrigen != null ? invOrigen.getSucursalNombre() : ""; }
    public String getDestino() { return invDestino != null ? invDestino.getSucursalNombre() : ""; }
    public int getCantidad() { return cantidad; }
    public Integer getCantidadAprobada() { return cantidadAprobada; }
    public String getEstado() { return estado; }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }

    public Inventario getInvOrigen() { return invOrigen; }
    public Inventario getInvDestino() { return invDestino; }
}
