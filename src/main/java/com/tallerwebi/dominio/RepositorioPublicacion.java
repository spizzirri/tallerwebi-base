package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;

import java.util.List;

public interface RepositorioPublicacion {

  Publicacion publicacion(String publicacion,Usuario usuario) throws UsuarioExistente;
    void realizada(Publicacion publicacion);
    Publicacion buscarPublicacionExistente (Publicacion publicacion);





}