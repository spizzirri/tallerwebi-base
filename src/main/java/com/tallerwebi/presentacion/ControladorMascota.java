package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Mascota;
import com.tallerwebi.dominio.ServicioPlan;
import com.tallerwebi.dominio.ServicioSocio;
import com.tallerwebi.dominio.Socio;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorMascota extends ControladorConSocio {

  private ServicioPlan servicioPlan;

  @Autowired
  public ControladorMascota(ServicioSocio servicioSocio, ServicioPlan servicioPlan) {
    super(servicioSocio);
    this.servicioPlan = servicioPlan;
  }

  @RequestMapping(path = "/mascotas/{id}", method = RequestMethod.GET)
  public ModelAndView verPerfil(@PathVariable("id") Long id, HttpSession sesion) {
    Socio socio = socioLogueado(sesion);
    Mascota mascota = socio == null ? null : socio.buscarMascota(id);
    if (mascota == null) {
      return new ModelAndView("redirect:/home");
    }
    Map<String, Object> modelo = new ModelMap();
    modelo.put("mascota", mascota);
    modelo.put("planes", servicioPlan.listar());
    return new ModelAndView("mascota-perfil", modelo);
  }
}
