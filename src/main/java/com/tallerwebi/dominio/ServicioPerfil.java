package com.tallerwebi.dominio;

public interface ServicioPerfil {
    Usuario obtenerPerfil(String email);
    void actualizarPerfil(Usuario usuario);
}