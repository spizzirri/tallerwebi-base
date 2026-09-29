package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioSocio;
import com.tallerwebi.dominio.Socio;
import jakarta.servlet.http.HttpSession;

/** Base para los controladores que trabajan con el socio que inició sesión. */
public class ControladorConSocio {

  private final ServicioSocio servicioSocio;

  protected ControladorConSocio(ServicioSocio servicioSocio) {
    this.servicioSocio = servicioSocio;
  }

  /** Devuelve el socio logueado, o null si el usuario no es socio (por ejemplo, el admin). */
  protected Socio socioLogueado(HttpSession sesion) {
    Object usuarioId = sesion.getAttribute(ControladorLogin.USUARIO_ID);
    return usuarioId instanceof Long ? servicioSocio.buscarPorUsuario((Long) usuarioId) : null;
  }
}
