package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Especie;
import com.tallerwebi.dominio.ServicioPlan;
import com.tallerwebi.dominio.ServicioRegistro;
import com.tallerwebi.dominio.excepcion.DatosDeRegistroInvalidos;
import com.tallerwebi.dominio.excepcion.SocioExistente;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorRegistro {

  private ServicioRegistro servicioRegistro;
  private ServicioPlan servicioPlan;

  @Autowired
  public ControladorRegistro(ServicioRegistro servicioRegistro, ServicioPlan servicioPlan) {
    this.servicioRegistro = servicioRegistro;
    this.servicioPlan = servicioPlan;
  }

  @RequestMapping(path = "/registro", method = RequestMethod.GET)
  public ModelAndView irARegistro() {
    return vistaRegistro(new DatosRegistro(), null);
  }

  @RequestMapping(path = "/registrarme", method = RequestMethod.POST)
  public ModelAndView registrarme(@ModelAttribute("datosRegistro") DatosRegistro datosRegistro) {
    try {
      servicioRegistro.registrarSocio(
        datosRegistro.crearSocio(),
        datosRegistro.crearMascota(),
        datosRegistro.getPlanId()
      );
    } catch (UsuarioExistente e) {
      return vistaRegistro(datosRegistro, "Ya existe una cuenta con ese email");
    } catch (SocioExistente e) {
      return vistaRegistro(datosRegistro, "Ya existe un socio con ese DNI");
    } catch (DatosDeRegistroInvalidos e) {
      return vistaRegistro(datosRegistro, e.getMessage());
    }
    return new ModelAndView("redirect:/login?registrado");
  }

  private ModelAndView vistaRegistro(DatosRegistro datosRegistro, String error) {
    Map<String, Object> modelo = new ModelMap();
    modelo.put("datosRegistro", datosRegistro);
    modelo.put("especies", Especie.values());
    modelo.put("planes", servicioPlan.listar());
    if (error != null) {
      modelo.put("error", error);
    }
    return new ModelAndView("registro", modelo);
  }
}
