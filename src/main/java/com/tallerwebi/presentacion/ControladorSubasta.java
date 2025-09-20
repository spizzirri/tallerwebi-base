package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ControladorSubasta {

    private ServicioSubasta servicioSubasta;

    @Autowired
    public  ControladorSubasta(ServicioSubasta servicioSubasta) {
        this.servicioSubasta = servicioSubasta;
    }

    //TO-DO: Mover estas clases de subastas a un controlador exclusivo, en vez de usar el de Login.
    @RequestMapping(path = "/nuevaSubasta", method = RequestMethod.GET)
    public ModelAndView irANuevaSubasta() {
        ModelMap model = new ModelMap();
        model.put("subasta", new Subasta());
        return new ModelAndView("nuevaSubasta", model);
    }



    @RequestMapping(path = "/crearSubasta", method = RequestMethod.POST)
    public ModelAndView crearSubasta(@ModelAttribute("subasta") Subasta subasta, HttpServletRequest request) {

        ModelMap model = new ModelMap();
        String creadorEmail = (String) request.getSession().getAttribute("USUARIO");
        try{
            servicioSubasta.crearSubasta(subasta, creadorEmail);
        }catch (Exception e){
            model.put("error", "Hubo un error al crear la subasta. Intentelo nuevamente.");
            return new ModelAndView("nuevaSubasta", model);
        }
        return new ModelAndView("redirect:/home");
    }
}
