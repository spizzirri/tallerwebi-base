package com.tallerwebi.presentacion;

import org.springframework.web.servlet.ModelAndView;

public class CartaDto {

    private String nombre;

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return this.nombre;
    }
}
