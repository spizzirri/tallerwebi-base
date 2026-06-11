package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioLogin")
@Transactional
public class ServicioLoginImpl implements ServicioLogin {

  private RepositorioUsuario repositorioUsuario;

  @Autowired
  public ServicioLoginImpl(RepositorioUsuario repositorioUsuario) {
    this.repositorioUsuario = repositorioUsuario;
  }

  @Override
  public Usuario buscar(String email) {
    return repositorioUsuario.buscar(email);
  }

  @Override
  public void registrar(Usuario usuario) throws UsuarioExistente {
    // Verificamos si ya existe un usuario con el mismo email para evitar duplicados
    Usuario usuarioEncontrado = repositorioUsuario.buscar(usuario.getEmail());

    if (usuarioEncontrado != null) {
      throw new UsuarioExistente();
    }
    // Si no existe, guardamos el nuevo usuario en la base de datos
    repositorioUsuario.guardar(usuario);
  }
}
