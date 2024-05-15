package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.calendario.ServicioCalendario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorCalendario {

    private ServicioCalendario servicioCalendario;

    @Autowired
    public ControladorCalendario(ServicioCalendario servicioCalendario) {
        this.servicioCalendario = servicioCalendario;
    }

    @RequestMapping(path = "/calendario", method = RequestMethod.GET)
    public ModelAndView irCalendario() {
        String viewNam = "calendario";
        ModelMap model = new ModelMap();
        model.put("message", "¿Cómo fue tu entrenamiento hoy?");//mensaje agregado
        return new ModelAndView(viewNam,model);
    }

    @RequestMapping(path = "/calendario", method = RequestMethod.GET)
    public ModelAndView guardarItemRendimientoSeleccionado() {
        String viewNam = "calendario";
        ModelMap model = new ModelMap();
        model.

    }
}
