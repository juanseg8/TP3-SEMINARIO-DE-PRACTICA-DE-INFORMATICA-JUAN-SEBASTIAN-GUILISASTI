package com.stockflow.service;

import com.stockflow.exception.AutenticacionException;
import com.stockflow.model.Usuario;
import com.stockflow.repository.UsuarioRepository;
import com.stockflow.repository.jdbc.JdbcUsuarioRepository;
import com.stockflow.session.SesionUsuario;

import java.util.Arrays;

public class AutenticacionService {
    private final UsuarioRepository usuarioRepository;

    public AutenticacionService() {
        this(new JdbcUsuarioRepository());
    }

    public AutenticacionService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario iniciarSesion(String nombreUsuario, char[] password) {
        if (nombreUsuario == null || nombreUsuario.isBlank() || password == null || password.length == 0) {
            throw new AutenticacionException("Ingrese usuario y contraseña.");
        }
        try {
            Usuario usuario = usuarioRepository.buscarActivoPorUsuario(nombreUsuario.trim())
                    .orElseThrow(() -> new AutenticacionException("Usuario o contraseña incorrectos."));
            if (!PasswordHasher.verificar(password, usuario.getClaveHash())) {
                throw new AutenticacionException("Usuario o contraseña incorrectos.");
            }
            SesionUsuario.iniciar(usuario);
            return usuario;
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    public void cerrarSesion() {
        SesionUsuario.cerrar();
    }
}
