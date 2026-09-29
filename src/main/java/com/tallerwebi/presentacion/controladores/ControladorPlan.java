package com.tallerwebi.presentacion.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorPlan {

  @RequestMapping(path = "/planes", method = RequestMethod.GET)
  public ModelAndView mostrarPlanes() {
    return new ModelAndView("planes");
  }
}
