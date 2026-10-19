package com.stockflow.repository;

import com.stockflow.model.Usuario;

import java.util.Optional;

public interface UsuarioRepository {
    Optional<Usuario> buscarActivoPorUsuario(String usuario);
}
