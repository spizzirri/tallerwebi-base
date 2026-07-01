package com.tallerwebi.dominio.Usuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioUsuarioOAuth {

  @Autowired
  private RepositorioUsuario repositorioUsuario;

  @Transactional
  public Usuario buscarOCrearUsuario(
    String email,
    String nombre,
    String apellido,
    String fotoPerfil
  ) {
    Usuario usuario = repositorioUsuario.buscarUsuarioPorEmail(email);
    if (usuario == null) {
      return crearNuevoUsuario(email, nombre, apellido, fotoPerfil);
    }

    actualizarFotoSiEsNecesario(usuario, fotoPerfil);
    return usuario;
  }

  //METODOS AUXILIARES!!
  private Usuario crearNuevoUsuario(
    String email,
    String nombre,
    String apellido,
    String fotoPerfil
  ) {
    Usuario nuevoUsuario = new Usuario();
    nuevoUsuario.setEmail(email);
    nuevoUsuario.setNombre(nombre != null ? nombre : "");
    nuevoUsuario.setApellido(apellido != null ? apellido : "");
    nuevoUsuario.setFotoPerfil(obtenerFotoValida(fotoPerfil));

    Long numeroTemporalUnico = System.currentTimeMillis();
    nuevoUsuario.setDni(numeroTemporalUnico);
    nuevoUsuario.setCelular(numeroTemporalUnico);
    nuevoUsuario.setRol("CLIENTE");
    nuevoUsuario.setActivo(true);

    repositorioUsuario.guardar(nuevoUsuario);
    return nuevoUsuario;
  }

  private void actualizarFotoSiEsNecesario(Usuario usuario, String fotoPerfil) {
    if (usuario.getFotoPerfil() == null || usuario.getFotoPerfil().trim().isEmpty()) {
      usuario.setFotoPerfil(obtenerFotoValida(fotoPerfil));
      repositorioUsuario.guardar(usuario);
    }
  }

  private String obtenerFotoValida(String fotoPerfil) {
    if (fotoPerfil != null && !fotoPerfil.trim().isEmpty()) {
      return fotoPerfil;
    }
    return "https://res.cloudinary.com/dqrka5zry/image/upload/v1780525781/default-user_lk0vzd.jpg";
  }
}
