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
    public ModelAndView agregarPublicacion(@ModelAttribute("publicacion") Publicacion publicacion,
                                           HttpServletRequest request) {
        ModelMap model = new ModelMap();
        try {
            Usuario usuario = (Usuario) request.getSession().getAttribute("usuarioLogueado");
            if (usuario != null) {
                publicacion.setUsuario(usuario); // <--- clave
            }
            servicioPublicado.realizar(publicacion);
            Publicacion publicada = servicioPublicado.publicacionEntera(publicacion.getDescripcion(), publicacion.getUsuario());
            model.put("publicacion", publicada);
            model.put("exito", "Publicación creada correctamente");
        } catch (Exception e) {
            model.put("error", "Error al publicar");
        }
        return new ModelAndView("redirect:/home");

    }

    @RequestMapping(path = "/nuevo-publicacion", method = RequestMethod.GET)
    public ModelAndView mostrarPublicacion() {
        ModelMap model = new ModelMap();
        model.put("publicacion", new Publicacion());
        return new ModelAndView("nueva-publicacion", model);
    }




}