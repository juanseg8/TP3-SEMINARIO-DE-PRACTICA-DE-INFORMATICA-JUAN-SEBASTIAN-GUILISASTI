package com.stockflow.model;

public class Usuario {
    private final int id;
    private final String nombre;
    private final String usuario;
    private final String claveHash;
    private final String rol;

    public Usuario(int id, String nombre, String usuario, String claveHash, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.usuario = usuario;
        this.claveHash = claveHash;
        this.rol = rol;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getUsuario() { return usuario; }
    public String getClaveHash() { return claveHash; }
    public String getRol() { return rol; }
}
