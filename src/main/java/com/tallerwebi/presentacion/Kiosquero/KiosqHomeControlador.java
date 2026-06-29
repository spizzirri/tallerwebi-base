package com.tallerwebi.presentacion.Kiosquero;

import com.tallerwebi.dominio.Usuario.Usuario;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class KiosqHomeControlador {

  @RequestMapping(path = "/homeKiosquero", method = RequestMethod.GET)
  public ModelAndView irAlHomeKiosquero(HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute("USUARIO");
    String rol = (String) session.getAttribute("ROL");

    // Si no está logueado o no es KIOSQUERO, afuera
    if (usuario == null || !"KIOSQUERO".equals(rol)) {
      return new ModelAndView("redirect:/login");
    }
    ModelMap modelo = new ModelMap();
    modelo.put("usuario", usuario);

    return new ModelAndView("homeKiosquero", modelo);
  }
}
