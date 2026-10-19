package com.stockflow.service;

import com.stockflow.exception.AutenticacionException;
import com.stockflow.model.ResumenInicio;
import com.stockflow.model.Usuario;
import com.stockflow.repository.ResumenRepository;
import com.stockflow.repository.jdbc.JdbcResumenRepository;
import com.stockflow.session.SesionUsuario;

public class InicioService {
    private final ResumenRepository repository;

    public InicioService() { this(new JdbcResumenRepository()); }

    public InicioService(ResumenRepository repository) {
        this.repository = repository;
    }

    public ResumenInicio obtenerResumen() {
        validarSesion();
        return repository.obtenerResumen();
    }

    public Usuario obtenerUsuarioActual() {
        validarSesion();
        return SesionUsuario.getUsuarioActual();
    }

    private void validarSesion() {
        if (!SesionUsuario.estaActiva()) {
            throw new AutenticacionException("La sesión finalizó. Vuelva a ingresar.");
        }
    }
}
