package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;

public interface ServicioPublicado {

    Publicacion publicacionEntera(String publicacion,Usuario usuario) throws UsuarioExistente;
    void realizar(Publicacion publicacion) throws PublicacionFallida;

}