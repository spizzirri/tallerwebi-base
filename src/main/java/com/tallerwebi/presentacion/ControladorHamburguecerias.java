package com.tallerwebi.presentacion;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorHamburguecerias {

    @RequestMapping(path = "/hamburguecerias-cercanas", method = RequestMethod.GET)
    public ModelAndView listarHamburguecerias(
            @RequestParam(required = false) Double latitud,
            @RequestParam(required = false) Double longitud) {
        ModelMap modelo = new ModelMap();
        
        // Agregar los parámetros al modelo para uso en la vista
        if (latitud != null) {
            modelo.put("latitud", latitud);
        }
        if (longitud != null) {
            modelo.put("longitud", longitud);
        }
        
        return new ModelAndView("hamburguecerias-cercanas", modelo);
    }
}