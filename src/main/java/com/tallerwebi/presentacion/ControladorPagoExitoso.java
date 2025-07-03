package com.tallerwebi.presentacion;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControladorPagoExitoso {

    @GetMapping("/pagoExitoso")
    public String mostrarPagoExitoso() {
        return "pagoExitoso";
    }
}