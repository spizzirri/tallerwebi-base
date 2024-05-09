package com.tallerwebi.presentacion;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@RequestMapping("/gym-cercano")
public class ControladorGymCercano {

    @RequestMapping(path = "/gym-cercano", method = RequestMethod.GET)
    public ModelAndView irGymCercano() {
        return new ModelAndView("gym-cercano");
    }

}


