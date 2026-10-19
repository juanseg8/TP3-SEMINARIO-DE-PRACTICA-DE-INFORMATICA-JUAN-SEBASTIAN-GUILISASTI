package com.stockflow.session;

import com.stockflow.model.Usuario;

public final class SesionUsuario {
    private static Usuario usuarioActual;

    private SesionUsuario() { }

    public static void iniciar(Usuario usuario) { usuarioActual = usuario; }
    public static Usuario getUsuarioActual() { return usuarioActual; }
    public static boolean estaActiva() { return usuarioActual != null; }
    public static void cerrar() { usuarioActual = null; }
}
