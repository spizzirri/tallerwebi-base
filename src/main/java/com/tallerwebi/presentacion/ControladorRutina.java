package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.rutina.ServicioRutina;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorRutina {

    private final ServicioRutina servicioRutina;

    @Autowired
    public ControladorRutina(ServicioRutina servicioRutina) {
        this.servicioRutina = servicioRutina;
    }

    @RequestMapping(path = "/rutina", method = RequestMethod.GET)
    public ModelAndView irRutina() {

        ModelMap model = new ModelMap();
        model.put("rutinas", this.servicioRutina.obtenerRutinas());

        return new ModelAndView("rutina", model);

    }
}



