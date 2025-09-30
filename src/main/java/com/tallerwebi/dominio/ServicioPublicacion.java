package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.springframework.stereotype.Service;


public interface ServicioPublicacion {

    Publicacion consultarPublicacion(String publicacion);
    void registrarpublicacion(String publicacion);
    long obtenerCantidadDeLikes(long id);
    int obtenerCantidadDeLikes(String publicacion);
}
