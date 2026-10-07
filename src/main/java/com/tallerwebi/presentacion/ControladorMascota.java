package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorMascota extends ControladorConSocio {

  private ServicioPlan servicioPlan;
  private ServicioMascota servicioMascota;

  @Autowired
  public ControladorMascota(
    ServicioSocio servicioSocio,
    ServicioPlan servicioPlan,
    ServicioMascota servicioMascota
  ) {
    super(servicioSocio);
    this.servicioPlan = servicioPlan;
    this.servicioMascota = servicioMascota;
  }

  @RequestMapping(path = "/mascotas/{id}", method = RequestMethod.GET)
  public ModelAndView verPerfil(@PathVariable("id") Long id, HttpSession sesion) {
    Socio socio = socioLogueado(sesion);
    Mascota mascota = servicioMascota.buscarPorIdParaSocio(socio, id);
    if (mascota == null) {
      return new ModelAndView("redirect:/home");
    }
    Map<String, Object> modelo = new ModelMap();
    modelo.put("mascota", mascota);
    modelo.put("planes", servicioPlan.listar());
    return new ModelAndView("mascota-perfil", modelo);
  }

  @RequestMapping(path = "/mascotas/{id}/cambiar-plan", method = RequestMethod.POST)
  public ModelAndView cambiarPlan(
    @PathVariable("id") Long id,
    @RequestParam("planId") Long planId,
    HttpSession sesion
  ) {
    servicioMascota.cambiarPlan(socioLogueado(sesion), id, planId);
    return new ModelAndView("redirect:/mascotas/" + id);
  }

  @RequestMapping(path = "/mascotas/{id}/baja", method = RequestMethod.POST)
  public ModelAndView darDeBaja(@PathVariable("id") Long id, HttpSession sesion) {
    servicioMascota.darDeBaja(socioLogueado(sesion), id);
    return new ModelAndView("redirect:/mascotas/" + id);
  }
}
