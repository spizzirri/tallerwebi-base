package com.tallerwebi.presentacion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.tallerwebi.dominio.ServicioVerHamburguesa;

@Controller
public class ControladorVerHamburguesa {
    private ServicioVerHamburguesa servicioVerHamburguesa;

    @Autowired
    public ControladorVerHamburguesa(ServicioVerHamburguesa sevicioVerHamburguesa) {
        this.servicioVerHamburguesa=sevicioVerHamburguesa;
    }

    @RequestMapping(path = "/hamburguesa", method = RequestMethod.GET)
    public static ModelAndView irAMostrarHamburguesa() {
      return new ModelAndView("hamburguesa");
    }

}
