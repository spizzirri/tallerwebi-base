package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;

import java.util.List;

public interface ServicioPublicado {

    Publicacion publicacionEntera(String publicacion,Usuario usuario) throws UsuarioExistente;
    void realizar(Publicacion publicacion) throws PublicacionFallida;
    List<Publicacion> findAll();
    Publicacion  obtenerPublicacionPorId(long id);

    int obtenerCantidadDeLikes(long id);

}