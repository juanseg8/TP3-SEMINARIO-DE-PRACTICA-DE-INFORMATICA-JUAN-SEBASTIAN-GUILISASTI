package com.stockflow.model;

public class Sucursal {
    private final int id;
    private final String nombre;

    public Sucursal(String nombre) {
        this(0, nombre);
    }

    public Sucursal(int id, String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la sucursal es obligatorio");
        }
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }

    @Override
    public String toString() { return nombre; }
}
