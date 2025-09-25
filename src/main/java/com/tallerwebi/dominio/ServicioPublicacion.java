package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;

public interface ServicioPublicacion {

    Publicacion consultarPublicacion(String publicacion);
    void registrarpublicacion(String publicacion);

}
