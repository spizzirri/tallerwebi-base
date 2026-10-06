package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Mascota;
import com.tallerwebi.dominio.ServicioMascota;
import com.tallerwebi.dominio.ServicioSocio;
import com.tallerwebi.dominio.Socio;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorPerfilMascota extends ControladorConSocio {

  private ServicioMascota servicioMascota;

  @Autowired
  public ControladorPerfilMascota(ServicioSocio servicioSocio, ServicioMascota servicioMascota) {
    super(servicioSocio);
    this.servicioMascota = servicioMascota;
  }

  @RequestMapping(path = "/perfil-mascota", method = RequestMethod.GET)
  public ModelAndView irAPerfilMascota(
    @RequestParam(name = "mascotaId", required = true) Long mascotaId,
    HttpSession sesion
  ) {
    Socio socio = socioLogueado(sesion);
    Mascota mascota = servicioMascota.buscarPorIdParaSocio(socio, mascotaId);

    if (mascota == null) {
      return new ModelAndView("redirect:/home");
    }

    return new ModelAndView("perfil-mascota", "mascota", mascota);
  }
}
