package com.stockflow.model;

import java.time.LocalDateTime;

public class Transferencia {
    private final int id;
    private final int recomendacionId;
    private final int inventarioOrigenId;
    private final int inventarioDestinoId;
    private final String productoNombre;
    private final String origen;
    private final String destino;
    private final int cantidad;
    private final String estado;
    private final LocalDateTime fechaRegistro;
    private final String responsable;

    public Transferencia(int id, int recomendacionId, int inventarioOrigenId, int inventarioDestinoId,
                         String productoNombre, String origen, String destino, int cantidad,
                         String estado, LocalDateTime fechaRegistro, String responsable) {
        this.id = id;
        this.recomendacionId = recomendacionId;
        this.inventarioOrigenId = inventarioOrigenId;
        this.inventarioDestinoId = inventarioDestinoId;
        this.productoNombre = productoNombre;
        this.origen = origen;
        this.destino = destino;
        this.cantidad = cantidad;
        this.estado = estado;
        this.fechaRegistro = fechaRegistro;
        this.responsable = responsable;
    }

    public int getId() { return id; }
    public int getRecomendacionId() { return recomendacionId; }
    public int getInventarioOrigenId() { return inventarioOrigenId; }
    public int getInventarioDestinoId() { return inventarioDestinoId; }
    public String getProductoNombre() { return productoNombre; }
    public String getOrigen() { return origen; }
    public String getDestino() { return destino; }
    public int getCantidad() { return cantidad; }
    public String getEstado() { return estado; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public String getResponsable() { return responsable; }
}
