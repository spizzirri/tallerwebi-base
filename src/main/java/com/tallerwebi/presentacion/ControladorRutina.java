package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.objetivo.Objetivo;
import com.tallerwebi.dominio.rutina.Rutina;
import com.tallerwebi.dominio.rutina.ServicioRutina;
import com.tallerwebi.dominio.rutina.ServicioRutinaImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class ControladorRutina {

    private ServicioRutina servicioRutina;

    @Autowired
    public ControladorRutina(ServicioRutina servicioRutina) {
        this.servicioRutina = servicioRutina;
    }

    @RequestMapping(path = "/rutina", method = RequestMethod.GET)
    public ModelAndView irRutina() {
        String viewName = "rutina";
        return new ModelAndView("rutina");
    }

    /*@RequestMapping(path = "/rutina?{objetivo}", method = RequestMethod.GET)
    public ModelAndView irARutinaDePerdidaDePeso(@PathVariable Objetivo objetivo)  {
        String viewName = "rutina";
        ModelMap model = new ModelMap();

        model.put("rutina",this.servicioRutina.getRutinaByObjetivo(objetivo));

        return new ModelAndView("rutina",model);
    }*/


    @RequestMapping(path = "/mi-rutina", method = RequestMethod.GET)
    public ModelAndView VerUnaRutinaEnLaPantallaRutina(Usuario usuario) {
        String viewName = "rutina";
        ModelMap model = new ModelMap();

        model.put("rutina",this.servicioRutina.getRutinaParaUsuario(usuario));

        return new ModelAndView("rutina", model);

    }

    @RequestMapping(path = "/rutinas", method = RequestMethod.GET)
    public ModelAndView VerRutinasQueLeInteresanAlUsuario(Usuario usuario) {
        ModelAndView modelAndView = new ModelAndView("rutinas");

        List<DatosRutina> rutinas = this.servicioRutina.getRutinasPorObjetivoDeUsuario(usuario);

        modelAndView.addObject("rutinas", rutinas);

        return modelAndView;

    }


}



