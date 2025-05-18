package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioJugador;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/inventario")
public class ControladorInventario {
    private final ServicioJugador servicioJugador;

    @Autowired
    public ControladorInventario(ServicioJugador servicioJugador) {
        this.servicioJugador = servicioJugador;
    }

    @GetMapping()
    public ModelAndView getInventario(HttpServletRequest request) {
        ModelMap model = new ModelMap();

        model.addAttribute("objetos", this.servicioJugador.getJugadorActual().getObjetos());

        return new ModelAndView("inventario", model);
    }
}
