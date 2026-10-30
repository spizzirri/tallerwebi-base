package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.ComentarioInvalido;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioComentario")
@Transactional
public class ServicioComentarioImpl implements ServicioComentario {

  private static final int MAX_PUNTOS_DE_CODIGO = 2000;
  private final RepositorioComentario repositorioComentario;

  @Autowired
  public ServicioComentarioImpl(RepositorioComentario repositorioComentario) {
    this.repositorioComentario = repositorioComentario;
  }

  @Override
  public void publicar(String contenido) {
    if (contenido == null || contenido.isBlank()) {
      throw new ComentarioInvalido("El comentario no puede estar vacío");
    }
    if (contenido.codePointCount(0, contenido.length()) > MAX_PUNTOS_DE_CODIGO) {
      throw new ComentarioInvalido("El comentario no puede superar los 2000 caracteres");
    }

    Comentario comentario = new Comentario();
    comentario.setContenido(contenido);
    this.repositorioComentario.guardar(comentario);
  }
}
