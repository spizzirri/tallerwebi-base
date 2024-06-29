package com.tallerwebi.presentacion;
import com.tallerwebi.dominio.alimentacion.ServicioAlimentacion;
import com.tallerwebi.dominio.usuario.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

@Controller
public class ControladorAlimentacion{

    private final ServicioAlimentacion servicioAlimentacion;

    @Autowired
    public ControladorAlimentacion(ServicioAlimentacion servicioAlimentacion) {
        this.servicioAlimentacion = servicioAlimentacion;
    }

    @RequestMapping(path = "/alimentacion", method = RequestMethod.GET)
    public ModelAndView irAlimentacion(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }

        ModelAndView model = new ModelAndView("alimentacion");
        model.addObject("usuario", usuario);
        
        return model;
    }

}
