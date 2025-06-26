package com.tallerwebi.presentacion;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("datosLogin")
    public DatosLoginDto datosLoginDto() {
        return new DatosLoginDto();
    }
}