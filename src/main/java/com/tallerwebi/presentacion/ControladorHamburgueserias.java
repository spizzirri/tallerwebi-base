package com.tallerwebi.presentacion;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.tallerwebi.dominio.Hamburgueseria;
import com.tallerwebi.dominio.ServicioCoordenadas;
import com.tallerwebi.dominio.ServicioHamburgueserias;

import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ControladorHamburgueserias {

    private ServicioCoordenadas servicioCoordenadas;
    private ServicioHamburgueserias servicioHamburgueserias;

    @Autowired
    public ControladorHamburgueserias(ServicioCoordenadas servicioCoordenadas, ServicioHamburgueserias servicioHamburgueserias){
        this.servicioCoordenadas = servicioCoordenadas;
        this.servicioHamburgueserias = servicioHamburgueserias;
    }

    @RequestMapping(path = "/hamburgueserias-cercanas", method = RequestMethod.GET)
    public ModelAndView irAHamburgueseriasCercanas() {
        ModelMap modelo = new ModelMap();
        return new ModelAndView("hamburgueserias-cercanas", modelo);
    }

    @RequestMapping(path = "/hamburgueserias-cercanas/{latitud}/{longitud}/lista", method = RequestMethod.GET)
    public ResponseEntity<ArrayList<Hamburgueseria>> listarhamburgueserias(
        @org.springframework.web.bind.annotation.PathVariable("latitud") Double latitud,
        @org.springframework.web.bind.annotation.PathVariable("longitud") Double longitud) {
        if (!servicioCoordenadas.validarLatitud(latitud) || !servicioCoordenadas.validarLongitud(longitud)) {
            System.out.println("Error: coordenadas inválidas. Latitud: " + latitud + ", Longitud: " + longitud);
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "Coordenadas inválidas"
            );
        }
        ArrayList<Hamburgueseria> hamburgueserias = servicioHamburgueserias.obtenerHamburgueseriasCercanas(latitud, longitud);
        return ResponseEntity.ok(hamburgueserias);
    }
} 