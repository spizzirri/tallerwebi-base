package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;

public interface RepositorioPublicacion {

    Publicacion publicacion(String publicacion,Usuario usuario) throws UsuarioExistente;
    void realizada(Publicacion publicacion);
    Publicacion buscarPublicacionExistente (Publicacion publicacion);
}