package com.tallerwebi.presentacion;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.tallerwebi.dominio.ServicioCaja;
import com.tallerwebi.dominio.excepcion.NoHayCajasExistente;


@Controller
@RequestMapping("/cajas")
public class ControladorCajas {

    private List<CajaDto> cajas;

    @Autowired
    private ServicioCaja servicioCaja;

    // public ControladorCajas(){
    // this.cajas = new ArrayList<>();
    // }
    
    public ControladorCajas(ServicioCaja servicioCaja) {
        this.cajas = new ArrayList<>();
        this.servicioCaja = servicioCaja;
    }
    
    @RequestMapping(path = "/", method = RequestMethod.GET)
    public ModelAndView mostrarCajas() {

        ModelMap model = new ModelMap();

        try {
            List<CajaDto> cajasDto = this.servicioCaja.obtener();
            model.put("cajas", cajasDto);
            model.put("exito", "Hay cajas.");
        } catch (NoHayCajasExistente e) {
            model.put("cajas", new ArrayList<>());
            model.put("error", "No hay cajas.");
        }

        return new ModelAndView("cajas", model);
    }

}
