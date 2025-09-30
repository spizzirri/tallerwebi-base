package com.tallerwebi.dominio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class RepositorioLikeImpl implements RepositorioLike {

    private final List<Like> likes = new ArrayList<>();

    @Override
    public boolean existePorUsuarioYPublicacion(Usuario usuario, Publicacion publicacion) {
        return likes.stream()
                .anyMatch(like -> like.getUsuario().equals(usuario) && like.getPublicacion().equals(publicacion));
    }

    @Override
    public Like encontrarPorUsuarioYPublicacion(Usuario usuario, Publicacion publicacion) {
        return likes.stream()
                .filter(like -> like.getUsuario().equals(usuario) && like.getPublicacion().equals(publicacion))
                .findFirst()
                .orElse(null);
    }

    @Override
    public int contarPorPublicacion(Publicacion publicacion) {
        return (int) likes.stream()
                .filter(like -> like.getPublicacion().equals(publicacion))
                .count();
    }

    @Override
    public Like guardar(Like like) {
        likes.add(like);
        return like;
    }

    @Override
    public Like eliminar(Like like) {
        likes.remove(like);
        return like;
    }
}
