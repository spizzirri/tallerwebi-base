package com.tallerwebi.dominio.Usuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioUsuarioOAuth {

  @Autowired
  private RepositorioUsuario repositorioUsuario;

  @Transactional
  public Usuario buscarOCrearUsuario(String email, String nombre, String apellido) {
    Usuario usuario = repositorioUsuario.buscarUsuarioPorEmail(email);
    if (usuario == null) {
      usuario = new Usuario();
      usuario.setEmail(email);
      usuario.setNombre(nombre != null ? nombre : "");
      usuario.setApellido(apellido != null ? apellido : "");
      usuario.setDni(0L);
      usuario.setCelular(0L);
      usuario.setRol("ROLE_USER");
      usuario.setActivo(true);
      repositorioUsuario.guardar(usuario);
    }
    return usuario;
  }
}
