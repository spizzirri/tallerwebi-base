package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.DatosDeRegistroInvalidos;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControladorMascota extends ControladorConSocio {

  private ServicioPlan servicioPlan;
  private ServicioMascota servicioMascota;
  private static final String REDIRECT_HOME = "redirect:/home";

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

  @RequestMapping(path = "/mascotas/nueva", method = RequestMethod.GET)
  public ModelAndView nuevaMascota(HttpSession sesion) {
    Socio socio = socioLogueado(sesion);
    if (socio == null) {
      return new ModelAndView(REDIRECT_HOME);
    }
    return vistaNuevaMascota(new Mascota(), null, null);
  }

  @RequestMapping(path = "/mascotas", method = RequestMethod.POST)
  public ModelAndView agregarMascota(
    @ModelAttribute("mascota") Mascota mascota,
    @RequestParam(value = "planId", required = false) Long planId,
    HttpSession sesion,
    RedirectAttributes redirectAttributes
  ) {
    Socio socio = socioLogueado(sesion);
    if (socio == null) {
      return new ModelAndView(REDIRECT_HOME);
    }
    try {
      servicioMascota.agregarMascota(socio, mascota, planId);
    } catch (DatosDeRegistroInvalidos e) {
      return vistaNuevaMascota(mascota, planId, e.getMessage());
    }
    redirectAttributes.addFlashAttribute("mensaje", "Agregaste una mascota.");
    return new ModelAndView(REDIRECT_HOME);
  }

  private ModelAndView vistaNuevaMascota(Mascota mascota, Long planId, String error) {
    Map<String, Object> modelo = new ModelMap();
    modelo.put("mascota", mascota);
    modelo.put("especies", Especie.values());
    modelo.put("planes", servicioPlan.listar());
    modelo.put("planId", planId);
    if (error != null) {
      modelo.put("error", error);
    }
    return new ModelAndView("nueva-mascota", modelo);
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
