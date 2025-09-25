package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorPublicacion {

    private ServicioPublicado servicioPublicado;

    @Autowired
    public ControladorPublicacion(ServicioPublicado servicioPublicado){
        this.servicioPublicado = servicioPublicado;
    }
    @RequestMapping(path = "/publicaciones/demo", method = RequestMethod.POST)
    public ModelAndView agregarPublicacion(@ModelAttribute("publicacion") Publicacion publicacion) {
        ModelMap model = new ModelMap();
        try {
            servicioPublicado.realizar(publicacion);
            Publicacion publicada = servicioPublicado.publicacionEntera(publicacion.getDescripcion(), publicacion.getUsuario());
            model.put("publicacion", publicada);
            model.put("exito", "Publicación creada correctamente");
        } catch (Exception e) {
            model.put("error", "Error al publicar");
        }
        return new ModelAndView("nuevo-publicacion", model);
    }

    @RequestMapping(path = "/nuevo-publicacion", method = RequestMethod.GET)
    public ModelAndView mostrarPublicacion() {
        ModelMap model = new ModelMap();
        model.put("publicacion", new Publicacion());
        return new ModelAndView("nuevo-publicacion", model);
    }
}