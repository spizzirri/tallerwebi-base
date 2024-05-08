package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Rutina;
import com.tallerwebi.dominio.ServicioRutina;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorRutina {
    private ServicioRutina servicioRutina;

    @RequestMapping(path = "/obtenerRutina", method = RequestMethod.POST)
    public ModelAndView obtenerRutina(@ModelAttribute("rutina") Rutina rutina) {
        return new ModelAndView("redirect:/vistaRutina");
    }
}
