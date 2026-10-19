package com.stockflow.model;

public class Producto {
    private final int id;
    private final String codigo;
    private final String nombre;

    public Producto(String nombre) {
        this(0, "", nombre);
    }

    public Producto(int id, String codigo, String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        this.id = id;
        this.codigo = codigo == null ? "" : codigo;
        this.nombre = nombre;
    }

    public int getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }

    @Override
    public String toString() { return nombre; }
}
