package com.stockflow.repository;

import com.stockflow.model.Recomendacion;

import java.util.List;

public interface RecomendacionRepository {
    List<Recomendacion> listar();
    int generarPendientes();
    void aprobar(int recomendacionId, int cantidad, int usuarioId, String motivo);
    void rechazar(int recomendacionId, int usuarioId, String motivo);
}
