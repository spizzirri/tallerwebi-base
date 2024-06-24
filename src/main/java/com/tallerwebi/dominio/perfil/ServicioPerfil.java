package com.tallerwebi.dominio.perfil;

import javax.transaction.Transactional;

public interface ServicioPerfil {
    @Transactional
    void guardarPerfil(Perfil perfil);

    @Transactional
    Perfil obtenerPerfilPorId(Long id);

    @Transactional
    void actualizarPerfil(Long idPerfil, Perfil perfilActualizado);

    String generarRecomendacion(Perfil perfil);
}
