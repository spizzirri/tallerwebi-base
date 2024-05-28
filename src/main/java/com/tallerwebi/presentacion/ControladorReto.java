package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.reto.Reto;
import com.tallerwebi.dominio.reto.ServicioReto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorReto {

    @Autowired
    private ServicioReto servicioReto;

    @Autowired
    private ServicioLogin servicioLogin;

    @Autowired
    public ControladorReto(ServicioReto servicioReto, ServicioLogin servicioLogin) {this.servicioReto = servicioReto;
        this.servicioLogin = servicioLogin;
    }

    @RequestMapping(path = "/empezar-reto", method = RequestMethod.POST)
    public ModelAndView empezarReto(@RequestParam Long retoId) {
        ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.addObject("retoId", retoId); // Añadir retoId al modelo
        try {
            servicioReto.empezarReto(retoId);

            // Añadir itemMasSeleccionado al modelo
            DatosItemRendimiento itemMasSeleccionado = servicioLogin.obtenerItemMasSeleccionado();
            modelAndView.addObject("itemMasSeleccionado", itemMasSeleccionado);

            // Añadir retoDisponible al modelo
            Reto retoDisponible = servicioReto.obtenerRetoDisponible();
            modelAndView.addObject("retoDisponible", retoDisponible);

        } catch (Exception e) {
            modelAndView.addObject("error", "An error occurred while starting the challenge: " + e.getMessage());
            modelAndView.setViewName("errorPage");
        }
        return modelAndView;
    }



}
