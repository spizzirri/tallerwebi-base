package com.tallerwebi.presentacion;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ControladorObjetivos {

    @PostMapping("/guardar-objetivos")
    public String guardarObjetivos(@RequestParam("objetivo") String objetivo) {
        System.out.println("Objetivo seleccionado: " + objetivo);
        return "redirect:/objetivos-guardados";
    }
}
