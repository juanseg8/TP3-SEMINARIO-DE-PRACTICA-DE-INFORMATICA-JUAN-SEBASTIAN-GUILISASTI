package com.stockflow.repository;

import com.stockflow.model.Transferencia;

import java.util.List;

public interface TransferenciaRepository {
    List<Transferencia> listar();
    void iniciar(int transferenciaId, int usuarioId);
    void completar(int transferenciaId, int usuarioId);
    void cancelar(int transferenciaId, int usuarioId, String motivo);
}
