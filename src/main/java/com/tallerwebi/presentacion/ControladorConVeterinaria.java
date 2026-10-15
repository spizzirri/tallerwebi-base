package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioVeterinaria;
import com.tallerwebi.dominio.Veterinaria;
import jakarta.servlet.http.HttpSession;

/** Base para las pantallas privadas de la veterinaria que inició sesión. */
public class ControladorConVeterinaria {

  private final ServicioVeterinaria servicioVeterinaria;

  protected ControladorConVeterinaria(ServicioVeterinaria servicioVeterinaria) {
    this.servicioVeterinaria = servicioVeterinaria;
  }

  protected Veterinaria veterinariaLogueada(HttpSession sesion) {
    if (
      !com.tallerwebi.dominio.Usuario.ROL_VETERINARIA.equals(
        sesion.getAttribute(ControladorLogin.ROL)
      )
    ) {
      return null;
    }
    Object usuarioId = sesion.getAttribute(ControladorLogin.USUARIO_ID);
    return usuarioId instanceof Long
      ? servicioVeterinaria.buscarPorUsuario((Long) usuarioId)
      : null;
  }
}
