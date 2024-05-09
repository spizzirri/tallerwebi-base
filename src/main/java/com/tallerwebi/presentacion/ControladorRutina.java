package com.tallerwebi.presentacion;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorRutina {

    @RequestMapping(path = "/rutina", method = RequestMethod.GET)
    public ModelAndView irRutina() {
        return new ModelAndView("rutina");
    }
}
