package com.tallerwebi.presentacion;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.tallerwebi.dominio.Hamburgueseria;
import com.tallerwebi.dominio.ServicioCoordenadas;
import com.tallerwebi.dominio.ServicioHamburgueserias;

@RestController
public class ControladorHamburgueserias {

    private ServicioCoordenadas servicioCoordenadas;
    private ServicioHamburgueserias servicioHamburgueserias;

    @Autowired
    public ControladorHamburgueserias(ServicioCoordenadas servicioCoordenadas,
            ServicioHamburgueserias servicioHamburgueserias) {
        this.servicioCoordenadas = servicioCoordenadas;
        this.servicioHamburgueserias = servicioHamburgueserias;
    }

    @RequestMapping("/hamburgueserias-cercanas")
    public ModelAndView irAHamburgueseriasCercanas() {
        ModelMap modelo = new ModelMap();
        return new ModelAndView("hamburgueserias-cercanas", modelo);
    }

    @RequestMapping(path = "/hamburgueserias-cercanas/{latitud}/{longitud}/lista", method = RequestMethod.GET)
    public ResponseEntity<List<HamburgueseriaCercana>> listarhamburgueserias(
            @org.springframework.web.bind.annotation.PathVariable("latitud") Double latitud,
            @org.springframework.web.bind.annotation.PathVariable("longitud") Double longitud) {
        if (!servicioCoordenadas.validarLatitud(latitud) || !servicioCoordenadas.validarLongitud(longitud)) {
            System.out.println("Error: coordenadas inválidas. Latitud: " + latitud + ", Longitud: " + longitud);
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Coordenadas inválidas");
        }

        List<HamburgueseriaCercana> hamburgueseriaCercanas = HamburgueseriasCercanasMapper
                .mapList(servicioHamburgueserias.obtenerHamburgueseriasCercanas(latitud,
                        longitud));

        return ResponseEntity.ok(hamburgueseriaCercanas);
    }
}