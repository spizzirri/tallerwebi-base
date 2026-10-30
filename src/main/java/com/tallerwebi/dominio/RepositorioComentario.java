package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioComentario {
  void guardar(Comentario comentario);

  List<Comentario> listarTodos();
}
