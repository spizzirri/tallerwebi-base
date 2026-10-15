package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioCobertura;
import com.tallerwebi.dominio.ServicioVeterinaria;
import com.tallerwebi.dominio.Veterinaria;
import com.tallerwebi.dominio.excepcion.DatosDeRegistroInvalidos;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import jakarta.servlet.http.HttpSession;
import java.time.DayOfWeek;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorRegistroVeterinaria {

  private final ServicioVeterinaria servicioVeterinaria;
  private final ServicioCobertura servicioCobertura;

  @Autowired
  public ControladorRegistroVeterinaria(
    ServicioVeterinaria servicioVeterinaria,
    ServicioCobertura servicioCobertura
  ) {
    this.servicioVeterinaria = servicioVeterinaria;
    this.servicioCobertura = servicioCobertura;
  }

  @RequestMapping(path = "/registro-veterinaria", method = RequestMethod.GET)
  public ModelAndView formularioRegistro() {
    return vistaRegistro(new DatosVeterinaria(), null);
  }

  @RequestMapping(path = "/registrar-veterinaria", method = RequestMethod.POST)
  public ModelAndView registrar(
    @ModelAttribute("datosVeterinaria") DatosVeterinaria datos,
    HttpSession sesion
  ) {
    Veterinaria veterinaria = datos.crearVeterinaria();
    try {
      servicioVeterinaria.registrar(veterinaria, datos.getServiciosIds());
    } catch (UsuarioExistente e) {
      return vistaRegistro(datos, "Ya existe una cuenta con ese email");
    } catch (DatosDeRegistroInvalidos e) {
      return vistaRegistro(datos, e.getMessage());
    }
    sesion.setAttribute(ControladorLogin.USUARIO_ID, veterinaria.getUsuario().getId());
    sesion.setAttribute(ControladorLogin.EMAIL, veterinaria.getUsuario().getEmail());
    sesion.setAttribute(ControladorLogin.ROL, veterinaria.getUsuario().getRol());
    return new ModelAndView("redirect:/veterinaria/inicio");
  }

  private ModelAndView vistaRegistro(DatosVeterinaria datos, String error) {
    Map<String, Object> modelo = new ModelMap();
    modelo.put("datosVeterinaria", datos);
    modelo.put("diasSemana", DayOfWeek.values());
    modelo.put("servicios", servicioCobertura.listarServicios());
    modelo.put("mostrarPassword", true);
    if (error != null) {
      modelo.put("error", error);
    }
    return new ModelAndView("registro-veterinaria", modelo);
  }
}
