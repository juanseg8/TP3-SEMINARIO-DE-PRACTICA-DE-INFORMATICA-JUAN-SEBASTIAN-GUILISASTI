package com.stockflow.service;

import com.stockflow.exception.AutenticacionException;
import com.stockflow.exception.TransferenciaException;
import com.stockflow.model.Transferencia;
import com.stockflow.repository.TransferenciaRepository;
import com.stockflow.repository.jdbc.JdbcTransferenciaRepository;
import com.stockflow.session.SesionUsuario;

import java.util.List;

public class TransferenciaService {
    private final TransferenciaRepository repository;

    public TransferenciaService() { this(new JdbcTransferenciaRepository()); }
    public TransferenciaService(TransferenciaRepository repository) { this.repository = repository; }

    public List<Transferencia> listar() { return repository.listar(); }

    public void iniciar(Transferencia transferencia) {
        validarSeleccion(transferencia);
        repository.iniciar(transferencia.getId(), usuarioId());
    }

    public void completar(Transferencia transferencia) {
        validarSeleccion(transferencia);
        repository.completar(transferencia.getId(), usuarioId());
    }

    public void cancelar(Transferencia transferencia, String motivo) {
        validarSeleccion(transferencia);
        repository.cancelar(transferencia.getId(), usuarioId(), motivo);
    }

    private void validarSeleccion(Transferencia transferencia) {
        if (transferencia == null) throw new TransferenciaException("Seleccione una transferencia.");
    }

    private int usuarioId() {
        if (!SesionUsuario.estaActiva()) throw new AutenticacionException("La sesión finalizó. Vuelva a ingresar.");
        return SesionUsuario.getUsuarioActual().getId();
    }
}
