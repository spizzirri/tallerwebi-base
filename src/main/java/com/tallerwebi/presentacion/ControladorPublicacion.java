package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ControladorPublicacion {

    private ServicioPublicado servicioPublicado;

    @Autowired
    public ControladorPublicacion(ServicioPublicado servicioPublicado){
        this.servicioPublicado = servicioPublicado;
    }

    @RequestMapping(path = "/publicaciones", method = RequestMethod.POST)
        public ModelAndView mostrarPublicacion(@ModelAttribute("publicacion") Publicacion publicacion) {
            ModelMap model = new ModelMap();
            try{
                servicioPublicado.realizar(publicacion);
            } catch (Exception e){
                model.put("error", "Error al publicar");
                return new ModelAndView("nueva-publicacion", model);
            }
            return new ModelAndView("redirect:/home");
        }

    @RequestMapping(path = "/nuevo-publicacion", method = RequestMethod.GET)
    public ModelAndView nuevaPublicacion() {
       ModelMap model = new ModelMap();
        model.put("publicacion", new Publicacion());
        return new ModelAndView("nuevo-publicacion", model);
    }
}