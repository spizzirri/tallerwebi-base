package com.tallerwebi.presentacion;

import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ControladorComponenteEspecifico {

    public ControladorComponenteEspecifico() {

        ComponenteEspecificoDto componenteEspecifico = new ComponenteEspecificoDto();

        componenteEspecifico.setId(1L);
        componenteEspecifico.setNombre("ComponenteEspecifico");
        componenteEspecifico.setPrecio(10.000);
        componenteEspecifico.setImagen("../fotos-producto/notebook1.jpg");

        List<String> cargaCaracteristicasGenerales = new ArrayList<>();
        cargaCaracteristicasGenerales.add("Ryzen 5 8600G\n");
        cargaCaracteristicasGenerales.add("AM5 Ryzen 8000\n");
        cargaCaracteristicasGenerales.add("4 nm\n");
        cargaCaracteristicasGenerales.add("AMD Radeon Graphics\n");
        cargaCaracteristicasGenerales.add("AMD RYZEN 5\n");
        componenteEspecifico.setCaracteristicasGenerales(cargaCaracteristicasGenerales);

        List<String> cargaEspecificacionesCPU = new ArrayList<>();
        cargaEspecificacionesCPU.add("6\n");
        cargaEspecificacionesCPU.add("4.300 mhz\n");
        cargaEspecificacionesCPU.add("12\n");
        cargaEspecificacionesCPU.add("5.000 mhz\n");
        componenteEspecifico.setEspecificacionesCPU(cargaEspecificacionesCPU);

        List<String> cargaCoolerYDisipadores = new ArrayList<>();
        cargaCoolerYDisipadores.add("Si\n");
        cargaCoolerYDisipadores.add("65 w\n");
        componenteEspecifico.setCoolersYDisipadores(cargaCoolerYDisipadores);

        List<String> cargaMemoria = new ArrayList<>();
        cargaMemoria.add("6 mb\n");
        cargaMemoria.add("16 mb\n");
        componenteEspecifico.setMemoria(cargaMemoria);

    }

}
