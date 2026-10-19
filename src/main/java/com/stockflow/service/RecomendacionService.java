package com.stockflow.service;

import com.stockflow.exception.AutenticacionException;
import com.stockflow.exception.ValidacionException;
import com.stockflow.model.Recomendacion;
import com.stockflow.model.Usuario;
import com.stockflow.repository.RecomendacionRepository;
import com.stockflow.repository.jdbc.JdbcRecomendacionRepository;
import com.stockflow.session.SesionUsuario;

import java.util.List;

public class RecomendacionService {
    private final RecomendacionRepository repository;

    public RecomendacionService() { this(new JdbcRecomendacionRepository()); }
    public RecomendacionService(RecomendacionRepository repository) { this.repository = repository; }

    public List<Recomendacion> listar() { return repository.listar(); }
    public int generarPendientes() { return repository.generarPendientes(); }

    public void aprobar(Recomendacion recomendacion, int cantidad, String motivo) {
        if (recomendacion == null) throw new ValidacionException("Seleccione una recomendación.");
        if (cantidad > recomendacion.getCantidad()) {
            throw new ValidacionException("La cantidad aprobada no puede superar la recomendada.");
        }
        repository.aprobar(recomendacion.getId(), cantidad, usuarioActual().getId(), motivo);
    }

    public void rechazar(Recomendacion recomendacion, String motivo) {
        if (recomendacion == null) throw new ValidacionException("Seleccione una recomendación.");
        repository.rechazar(recomendacion.getId(), usuarioActual().getId(), motivo);
    }

    private Usuario usuarioActual() {
        if (!SesionUsuario.estaActiva()) throw new AutenticacionException("La sesión finalizó. Vuelva a ingresar.");
        return SesionUsuario.getUsuarioActual();
    }
}
