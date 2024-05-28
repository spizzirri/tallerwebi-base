package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.reto.ServicioReto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/reto")
public class ControladorReto {

    @Autowired
    private ServicioReto servicioReto;

    @Autowired
    public ControladorReto(ServicioReto servicioReto) {this.servicioReto = servicioReto;
    }

    @GetMapping("/home/empezar-reto")
    public ModelAndView empezarReto(@RequestParam Long retoId) {
        ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.addObject("retoId", retoId); // Añadir retoId al modelo
        try {
            servicioReto.empezarReto(retoId);
        } catch (Exception e) {
            modelAndView.addObject("error", "An error occurred while starting the challenge: " + e.getMessage());
            modelAndView.setViewName("errorPage"); // Asumiendo que tienes una página de error
        }
        return modelAndView;
    }


}
