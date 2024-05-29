package com.tallerwebi.presentacion;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorPerfil{

    @RequestMapping(path = "/perfil", method = RequestMethod.GET)
    public ModelAndView irPerfil() {
        return new ModelAndView("perfil");
    }

}
