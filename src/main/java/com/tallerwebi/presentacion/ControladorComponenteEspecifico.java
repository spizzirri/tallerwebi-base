package com.tallerwebi.presentacion;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
public class ControladorComponenteEspecifico {

    ComponenteEspecificoDto componenteEspecifico = new ComponenteEspecificoDto();

    public ControladorComponenteEspecifico() {

        componenteEspecifico.setId(1L);
        componenteEspecifico.setNombre("ComponenteEspecifico");
        componenteEspecifico.setPrecio(10000.0);

        List<String> imagenes = new ArrayList<>();
        imagenes.add("procesador1.jpg");
        imagenes.add("procesador2.jpg");
        componenteEspecifico.setImagenes(imagenes);

        Map<String, String> cargaCaracteristicasGenerales = new LinkedHashMap<>();
        cargaCaracteristicasGenerales.put("Modelo", "Ryzen 5 8600G\n");
        cargaCaracteristicasGenerales.put("Socket", "AM5 Ryzen 8000\n");
        cargaCaracteristicasGenerales.put("Proceso de Fabricacion", "4 nm\n");
        cargaCaracteristicasGenerales.put("Chipset GPU", "AMD Radeon Graphics\n");
        cargaCaracteristicasGenerales.put("Familia", "AMD RYZEN 5\n");
        componenteEspecifico.setCaracteristicasGenerales(cargaCaracteristicasGenerales);

        Map<String, String> cargaEspecificacionesCPU = new LinkedHashMap<>();
        cargaEspecificacionesCPU.put("Nucleos", "6\n");
        cargaEspecificacionesCPU.put("Frecuencia", "4.300 mhz\n");
        cargaEspecificacionesCPU.put("Hilos", "12\n");
        cargaEspecificacionesCPU.put("Frecuencia Turbo", "5.000 mhz\n");
        componenteEspecifico.setEspecificacionesCPU(cargaEspecificacionesCPU);

        Map<String, String> cargaCoolerYDisipadores = new LinkedHashMap<>();
        cargaCoolerYDisipadores.put("Incluye Cooler CPU", "Si\n");
        cargaCoolerYDisipadores.put("TDP Predeterminada", "65 w\n");
        componenteEspecifico.setCoolersYDisipadores(cargaCoolerYDisipadores);

        Map<String, String> cargaMemoria = new LinkedHashMap<>();
        cargaMemoria.put("L2", "6 mb\n");
        cargaMemoria.put("L3", "16 mb\n");
        componenteEspecifico.setMemoria(cargaMemoria);

    }

    @GetMapping(path = "productoEspecifico/{id}")
    public ModelAndView mostrarComponenteEspecifico(@PathVariable Long id) {

        ModelMap model = new ModelMap();

        model.put("componenteEspecificoDto", componenteEspecifico);
        model.put("terminos", "Estos son los terminos y condiciones para este producto...");
        model.put("cuotas", "12 cuotas fijas sin interes con tarjetas seleccionadas.");

        return new ModelAndView("productoEspecifico", model);
    }

    @PostMapping(path = "productoEspecifico/{id}")
    public String guardarComponenteEspecifico(@PathVariable Long id,
                                              @ModelAttribute ComponenteEspecificoDto componenteEspecificoDto,
                                              HttpSession session) {

        session.setAttribute("componenteEspecificoGuardado", componenteEspecificoDto);

        return "redirect:/productoEspecifico/" + id;
    }


}
