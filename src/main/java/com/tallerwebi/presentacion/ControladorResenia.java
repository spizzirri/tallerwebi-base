package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Resenia;
import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.ServicioResenia;
import com.tallerwebi.dominio.excepcion.ReseniaInvalida;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ControladorResenia {
    private ServicioResenia servicioResenia;

    @Autowired
    public ControladorResenia(ServicioResenia servicioResenia) {this.servicioResenia = servicioResenia;}

    @RequestMapping("/resenia")
    public ModelAndView irAReseniar(){
        ModelMap modelo = new ModelMap();
        modelo.put("datosResenia", new DatosResenia());
        return new ModelAndView("resenia", modelo);
    }

    @RequestMapping(path = "/resenia/crear", method = RequestMethod.POST)
    public ModelAndView crearResenia(@ModelAttribute("resenia") Resenia resenia, HttpServletRequest request) {
        ModelMap model = new ModelMap();
        servicioResenia.crearResenia(resenia);
        return new ModelAndView("redirect:/home", model);
    }
}
