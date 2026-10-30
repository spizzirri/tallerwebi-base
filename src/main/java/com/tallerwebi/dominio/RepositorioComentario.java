package com.tallerwebi.dominio;

@FunctionalInterface
public interface RepositorioComentario {
  void guardar(Comentario comentario);
}
