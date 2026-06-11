package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioLogin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controlador encargado de gestionar las rutas relacionadas con la autenticación
 * y navegación inicial del usuario.
 */
@Controller
public class ControladorLogin {

  private ServicioLogin servicioLogin;

  @Autowired
  public ControladorLogin(ServicioLogin servicioLogin) {
    this.servicioLogin = servicioLogin;
  }

  /**
   * Muestra la vista de inicio de sesión para el usuario.
   *
   * @return un objeto {@link ModelAndView} con la vista "login" y los datos necesarios
   */
  @RequestMapping("/login")
  public ModelAndView irALogin() {
    ModelMap modelo = new ModelMap();
    modelo.put("datosLogin", new DatosLogin());
    return new ModelAndView("login", modelo);
  }

  /**
   * Muestra la vista de la página principal una vez autenticado el usuario.
   *
   * @return un objeto {@link ModelAndView} con la vista "home"
   */
  @RequestMapping(path = "/home", method = RequestMethod.GET)
  public ModelAndView irAHome() {
    return new ModelAndView("home");
  }

  /**
   * Punto de entrada de la aplicación, redirige automáticamente a la pantalla de login.
   *
   * @return un objeto {@link ModelAndView} con una redirección a "/login"
   */
  @RequestMapping(path = "/", method = RequestMethod.GET)
  public ModelAndView inicio() {
    return new ModelAndView("redirect:/login");
  }
}
