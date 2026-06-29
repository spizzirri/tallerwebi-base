package com.tallerwebi.dominio.Usuario;

import com.tallerwebi.dominio.Carrito.RepositorioCarrito;
import com.tallerwebi.dominio.Hijos.RepositorioHijo;
import com.tallerwebi.dominio.Pedidos.RepositorioPedido;
import com.tallerwebi.dominio.SubidaDeImgs.ServicioImagenes;
import com.tallerwebi.dominio.excepcion.NoSePudoGuardarInformacionException;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class ServicioUsuarioImpl implements ServicioUsuario {

  RepositorioUsuario repositorioUsuario;
  ServicioImagenes servicioImagenes;
  RepositorioHijo repositorioHijo;
  RepositorioCarrito repositorioCarrito;
  RepositorioPedido repositorioPedido;

  @Autowired
  public ServicioUsuarioImpl(
    RepositorioUsuario repositorioUsuario,
    ServicioImagenes servicioImagenes,
    RepositorioHijo repositorioHijo,
    RepositorioCarrito repositorioCarrito,
    RepositorioPedido repositorioPedido
  ) {
    this.repositorioUsuario = repositorioUsuario;
    this.servicioImagenes = servicioImagenes;
    this.repositorioHijo = repositorioHijo;
    this.repositorioCarrito = repositorioCarrito;
    this.repositorioPedido = repositorioPedido;
  }

  @Override
  public void actualizarMail(Long id, String mailNuevo) {
    Usuario usuario = repositorioUsuario.buscarUsuarioPorId(id);
    usuario.setEmail(mailNuevo);
    try {
      repositorioUsuario.modificar(usuario);
    } catch (Exception e) {
      throw new NoSePudoGuardarInformacionException("No se pudo guardar el nuevo mail ", e);
    }
  }

  @Override
  public void actualizarCelular(Long id, Long celular) {
    Usuario usuario = repositorioUsuario.buscarUsuarioPorId(id);
    usuario.setCelular(celular);
    try {
      repositorioUsuario.modificar(usuario);
    } catch (Exception e) {
      throw new NoSePudoGuardarInformacionException("No se pudo guardar el nuevo celular ", e);
    }
  }

  @Override
  public void actualizarFoto(Long id, MultipartFile fotoPerfil) {
    try {
      Usuario usuario = repositorioUsuario.buscarUsuarioPorId(id);

      String rutaGuardarEnHosting = servicioImagenes.subirImagen(
        fotoPerfil,
        "KionetTWI/img_perfiles"
      );

      usuario.setFotoPerfil(rutaGuardarEnHosting);

      repositorioUsuario.modificar(usuario);
    } catch (Exception e) {
      throw new NoSePudoGuardarInformacionException(
        "No se pudo guardar la nueva foto de perfil",
        e
      );
    }
  }

  @Override
  public Usuario buscarPorId(Long id) {
    return repositorioUsuario.buscarUsuarioPorId(id);
  }

  @Override
  public void eliminarCuenta(Long id) {
    repositorioHijo.eliminarPorUsuario(id);
    repositorioCarrito.eliminarPorUsuario(id);
    repositorioPedido.eliminarPorUsuario(id);
    Usuario usuario = repositorioUsuario.buscarUsuarioPorId(id);
    if (usuario != null) {
      repositorioUsuario.eliminar(usuario);
    }
  }
}
