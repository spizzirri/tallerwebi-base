package com.tallerwebi.dominio.perfil;

import javax.transaction.Transactional;

public interface RepositorioPerfil {

    Perfil obtenerPerfilPorId(Long id);
    void guardarPerfil(Perfil perfil);

    @Transactional
    void actualizarPerfil(Perfil perfilActualizado);
}
