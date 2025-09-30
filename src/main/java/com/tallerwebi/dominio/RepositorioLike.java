package com.tallerwebi.dominio;

import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioLike {

    boolean existePorUsuarioYPublicacion(Usuario usuario, Publicacion publicacion);
    Like encontrarPorUsuarioYPublicacion(Usuario usuario, Publicacion publicacion);
    int contarPorPublicacion(Publicacion publicacion);
    Like guardar (Like like);
    Like eliminar(Like like);

    }

