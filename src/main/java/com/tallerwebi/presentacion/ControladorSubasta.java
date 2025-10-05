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
import java.util.List;

@Controller
public class ControladorSubasta {

    private ServicioSubasta servicioSubasta;

    @Autowired
    public  ControladorSubasta(ServicioSubasta servicioSubasta) {
        this.servicioSubasta = servicioSubasta;
    }

    @RequestMapping(path = "/nuevaSubasta", method = RequestMethod.GET)
    public ModelAndView irANuevaSubasta() {
        ModelMap model = new ModelMap();
        model.put("subasta", new Subasta());
        List<Categorias> cat = servicioSubasta.listarCategoriasDisponibles();
        model.put("listaCategorias", cat);
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
