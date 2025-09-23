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
import com.tallerwebi.dominio.ServicioCoordenadasImpl;
import com.tallerwebi.dominio.ServicioHamburgueseriasImpl;

@RestController
public class ControladorHamburgueserias {

    private ServicioCoordenadasImpl servicioCoordenadas;
    private ServicioHamburgueseriasImpl servicioHamburgueserias;

    @Autowired
    public ControladorHamburgueserias(ServicioCoordenadasImpl servicioCoordenadas, ServicioHamburgueseriasImpl servicioHamburgueserias){
        this.servicioCoordenadas = servicioCoordenadas;
        this.servicioHamburgueserias = servicioHamburgueserias;
    }

    @RequestMapping(path = "/hamburgueserias-cercanas", method = RequestMethod.GET)
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
                    "Coordenadas inválidas"
                );
            }
            
            List<Hamburgueseria> hamburgueserias = servicioHamburgueserias.obtenerHamburgueseriasCercanas(latitud, longitud);
            List<HamburgueseriaCercana> dtos = hamburgueserias.stream()
                .map(h -> new HamburgueseriaCercana(
                    h.getId(),
                    h.getNombre(),
                    h.getLatitud(),
                    h.getLongitud(),
                    h.getPuntuacion()
                ))
                .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        }
} 