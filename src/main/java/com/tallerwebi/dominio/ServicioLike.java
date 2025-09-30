package com.tallerwebi.dominio;


import org.springframework.stereotype.Service;


public interface ServicioLike {
    void darLike(Usuario usuario, Publicacion publicacion);
    void quitarLike(Usuario usuario, Publicacion publicacion);
    boolean yaDioLike(Usuario usuario, Publicacion publicacion);
    int contarLikes(Publicacion publicacion);
}



