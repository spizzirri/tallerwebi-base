package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioComentario {
  void publicar(String contenido);

  List<Comentario> listar();
}
