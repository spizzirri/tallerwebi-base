package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioCarta;
import com.tallerwebi.presentacion.dtos.CartaDto;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;


public class ControladorCarta {

    private ServicioCarta servicioCarta;

    public ControladorCarta(ServicioCarta servicioCarta) {
        this.servicioCarta = servicioCarta;
    }

    public ModelAndView crearCarta(CartaDto carta) {

        ModelMap modelMap = new ModelMap();

        Boolean creada = this.servicioCarta.crear(carta);
        String mensaje = "Error al crear carta";

        if(creada) {
            mensaje = "Carta creada correctamente";
        }

        modelMap.put("mensaje", mensaje);
        modelMap.put("carta", carta);

        return new ModelAndView("crear-carta", modelMap);
    }
}
