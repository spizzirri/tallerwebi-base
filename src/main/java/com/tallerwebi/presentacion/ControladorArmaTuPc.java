package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioArmaTuPc;
import com.tallerwebi.dominio.excepcion.LimiteDeComponenteSobrepasadoEnElArmadoException;
import com.tallerwebi.presentacion.dto.ArmadoPcDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
public class ControladorArmaTuPc {

    private ServicioArmaTuPc servicioArmaTuPc;
    private static final Map<String, String> pasosYCorrespondenciaEnBD = new LinkedHashMap<>() {{
        put("procesador", "Procesador");
        put("motherboard", "Motherboard");
        put("cooler", "CoolerCPU");
        put("memoria", "MemoriaRAM");
        put("gpu", "PlacaDeVideo");
        put("almacenamiento", "Almacenamiento");
        put("fuente", "FuenteDeAlimentacion");
        put("gabinete", "Gabinete");
        put("monitor", "Componente");
        put("periferico", "Componente");
        put("resumen", null); // ahora sí es válido
    }};

    @Autowired
    public ControladorArmaTuPc(ServicioArmaTuPc servicioArmaTuPc) { this.servicioArmaTuPc =  servicioArmaTuPc; }
    public ControladorArmaTuPc() {}

     private ArmadoPcDto obtenerArmadoPcDtoCorrespondiente(HttpSession session) {
        if(session.getAttribute("armadoPcDto") == null){ // si no existe el armado de pc en la session, se la creo
            ArmadoPcDto armadoPcDto = new ArmadoPcDto();
            session.setAttribute("armadoPcDto", armadoPcDto);
        }// si ya existia entonces lo rescatamos de la sesion
        return (ArmadoPcDto) session.getAttribute("armadoPcDto");
    }


    @RequestMapping(path = "arma-tu-pc/tradicional/{tipoComponente}", method = RequestMethod.GET)
    public ModelAndView cargarComponentes(@PathVariable("tipoComponente") String tipoComponente, HttpSession sesion) {

        ModelMap model = new ModelMap();
        model.put(tipoComponente+"Lista", this.servicioArmaTuPc.obtenerListaDeComponentesDto(this.pasosYCorrespondenciaEnBD.get(tipoComponente)));
        model.put("armadoPcDto", obtenerArmadoPcDtoCorrespondiente(sesion));

        return new ModelAndView("arma-tu-pc/tradicional/" + tipoComponente, model);
    }

    @RequestMapping(path = "arma-tu-pc/tradicional/{tipoComponente}", method = RequestMethod.POST)
    public ModelAndView agregarComponenteAlArmado(@PathVariable("tipoComponente")String tipoComponente,
                                                  @RequestParam("id") Long idComponente,
                                                  @RequestParam("cantidad") Integer cantidad,
                                                  HttpSession session) {

        ArmadoPcDto armadoPcDtoConComponenteAgregado = null;
        try {
            armadoPcDtoConComponenteAgregado = this.servicioArmaTuPc.agregarComponenteAlArmado(idComponente, tipoComponente, cantidad, obtenerArmadoPcDtoCorrespondiente(session));
        } catch (LimiteDeComponenteSobrepasadoEnElArmadoException e) {

            ModelMap model = new ModelMap();
            model.put("errorLimite", "Supero el limite de "+tipoComponente+" de su armado");
            return new ModelAndView("redirect:/arma-tu-pc/tradicional/" + tipoComponente, model);
        }

        session.setAttribute("armadoPcDto", armadoPcDtoConComponenteAgregado);

        String siguienteVista = determinarSiguienteVista(tipoComponente, armadoPcDtoConComponenteAgregado);
        return new ModelAndView("redirect:/arma-tu-pc/tradicional/" + siguienteVista);
    }

    private String determinarSiguienteVista(String tipoComponente, ArmadoPcDto armadoPcDto) {

        if(this.servicioArmaTuPc.sePuedeAgregarMasUnidades(tipoComponente, armadoPcDto)) return tipoComponente;

        List<String> pasos = new ArrayList<>(this.pasosYCorrespondenciaEnBD.keySet());

        return pasos.get(pasos.indexOf(tipoComponente)+1);
    }

    @RequestMapping(path = "arma-tu-pc/tradicional/resumen", method = RequestMethod.GET)
    public ModelAndView obtenerResumen(HttpSession session) {
        ModelMap model = new ModelMap();

        ArmadoPcDto armadoPcDto = obtenerArmadoPcDtoCorrespondiente(session);

        if(this.servicioArmaTuPc.armadoCompleto(armadoPcDto)) model.put("armadoPcDto", armadoPcDto);
        else model.put("errorResumen", "Seleccione almenos un motherboard, cpu, cooler y gabinete para obtener su armado");

        return new ModelAndView("arma-tu-pc/tradicional/resumen", model);
    }

    @RequestMapping(path = "arma-tu-pc/tradicional/reiniciar-armado", method = RequestMethod.POST)
    public ModelAndView reiniciarArmado(HttpSession session, HttpServletRequest request) {

        session.removeAttribute("armadoPcDto");

        return new ModelAndView("redirect:/arma-tu-pc/tradicional/procesador");
    }
}
