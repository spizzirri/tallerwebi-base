package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioArmaTuPc;
import com.tallerwebi.dominio.ServicioPrecios;
import com.tallerwebi.dominio.entidades.Componente;
import com.tallerwebi.dominio.excepcion.ComponenteDeterminateDelArmadoEnNullException;
import com.tallerwebi.dominio.excepcion.LimiteDeComponenteSobrepasadoEnElArmadoException;
import com.tallerwebi.dominio.excepcion.QuitarComponenteInvalidoException;
import com.tallerwebi.dominio.excepcion.QuitarStockDemasDeComponenteException;
import com.tallerwebi.presentacion.dto.ArmadoPcDto;
import com.tallerwebi.presentacion.dto.ComponenteDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.*;

@Controller
public class ControladorArmaTuPc {

    private ServicioArmaTuPc servicioArmaTuPc;
    private ServicioPrecios servicioPrecios;
    private List<String> pasos = Arrays.asList("procesador", "motherboard", "cooler", "memoria", "gpu", "almacenamiento", "fuente", "gabinete", "monitor", "periferico", "resumen");

    @Autowired
    public ControladorArmaTuPc(ServicioArmaTuPc servicioArmaTuPc, ServicioPrecios servicioPrecios) {this.servicioArmaTuPc =  servicioArmaTuPc;this.servicioPrecios = servicioPrecios;}
    public ControladorArmaTuPc() {}

     private ArmadoPcDto obtenerArmadoPcDtoDeLaSession(HttpSession session) {
        if(session.getAttribute("armadoPcDto") == null){ // si no existe el armado de pc en la session, se la creo
            ArmadoPcDto armadoPcDto = new ArmadoPcDto();
            session.setAttribute("armadoPcDto", armadoPcDto);
        }// si ya existia entonces lo rescatamos de la sesion
        return (ArmadoPcDto) session.getAttribute("armadoPcDto");
    }

    @RequestMapping(path = "arma-tu-pc/tradicional/{tipoComponente}", method = RequestMethod.GET)
    public ModelAndView cargarComponentes(@PathVariable("tipoComponente") String tipoComponente,
                                          @RequestParam(value = "q", required = false) String query,
                                          HttpSession sesion) {

        ModelMap model = new ModelMap();
        ArmadoPcDto armadoPcDto = obtenerArmadoPcDtoDeLaSession(sesion);
        try {
            List<ComponenteDto> componentesCompatiblesADevolver;

            componentesCompatiblesADevolver = (query != null)
                    ? this.servicioArmaTuPc.obtenerListaDeComponentesCompatiblesFiltradosDto(tipoComponente, query, armadoPcDto)
                    : this.servicioArmaTuPc.obtenerListaDeComponentesCompatiblesDto(tipoComponente, armadoPcDto);

            model.put("componentesLista", this.pasarPreciosAPesos(componentesCompatiblesADevolver));

        } catch (ComponenteDeterminateDelArmadoEnNullException e) {
            model.put("errorLista", e.getMessage());
        }
        model.put("armadoPcDto", armadoPcDto);
        model.put("idsDeComponentesSeleccionados", obtenerIdsDeArmadoDeSession(armadoPcDto));

        //falta determinar si ingresa a un paso que no existe

        model.put("pasoActual", tipoComponente);
        model.put("pasoAnterior", obtenerPasoAnterior(tipoComponente));
        model.put("pasoSiguiente", obtenerPasoSiguiente(tipoComponente));

        return new ModelAndView("arma-tu-pc/tradicional/" + tipoComponente, model);
    }

    private List<ComponenteDto> pasarPreciosAPesos(List<ComponenteDto> componentesCompatiblesADevolver) {

        for (ComponenteDto componente : componentesCompatiblesADevolver) {
            if (componente != null) componente.setPrecioFormateado(this.servicioPrecios.conversionDolarAPeso(componente.getPrecio()));
        }
        return componentesCompatiblesADevolver;
    }

    private String obtenerPasoSiguiente(String tipoComponente) {
        if (this.pasos.indexOf(tipoComponente) >= this.pasos.size()-1) return null;
        return this.pasos.get(this.pasos.indexOf(tipoComponente) + 1);
    }

    private String obtenerPasoAnterior(String tipoComponente) {
        if (this.pasos.indexOf(tipoComponente) <= 0) return null;
        return this.pasos.get(this.pasos.indexOf(tipoComponente) - 1);
    }

    private Set<Long> obtenerIdsDeArmadoDeSession(ArmadoPcDto armadoPcDto) {
        Set<Long> idsDeArmadoDeSession = new HashSet<>();
        for(ComponenteDto componente : armadoPcDto.getComponentesDto())
            if(componente != null && componente.getId() != null)
                idsDeArmadoDeSession.add(componente.getId());
        return idsDeArmadoDeSession;
    }

    @RequestMapping(path = "arma-tu-pc/tradicional/{tipoComponente}/accion", method = RequestMethod.POST)
    public ModelAndView procesarAccion(@PathVariable("tipoComponente")String tipoComponente,
                                       @RequestParam("accion")String accion,
                                       @RequestParam("id")Long idComponente,
                                       @RequestParam("cantidad")Integer cantidad,
                                       HttpSession session){

        switch(accion.toLowerCase()){
            case "agregar":
                return this.agregarComponenteAlArmado(tipoComponente, idComponente, cantidad, session);
            case "quitar":
                return this.quitarComponenteDelArmado(tipoComponente, idComponente, cantidad, session);
        }

        ModelMap model = new ModelMap();
        model.put("accionInvalida", "Ingreso una accion invalida.");

        return new ModelAndView("redirect:/arma-tu-pc/tradicional/" + tipoComponente, model);
    }

    public ModelAndView agregarComponenteAlArmado(@PathVariable("tipoComponente")String tipoComponente,
                                                  @RequestParam("id") Long idComponente,
                                                  @RequestParam("cantidad") Integer cantidad,
                                                  HttpSession session) {
        ModelMap model = new ModelMap();
        ArmadoPcDto armadoPcDtoConComponenteAgregado = null;
        try {
            armadoPcDtoConComponenteAgregado = this.servicioArmaTuPc.agregarComponenteAlArmado(idComponente, tipoComponente, cantidad, obtenerArmadoPcDtoDeLaSession(session));
        } catch (LimiteDeComponenteSobrepasadoEnElArmadoException e) {
            model.put("errorLimite", "Supero el limite de "+tipoComponente+" de su armado");
            return new ModelAndView("redirect:/arma-tu-pc/tradicional/" + tipoComponente, model);
        }

        armadoPcDtoConComponenteAgregado.setPrecioFormateado(
                this.servicioPrecios.conversionDolarAPeso(armadoPcDtoConComponenteAgregado.getPrecioTotal())
                );

        session.setAttribute("armadoPcDto", armadoPcDtoConComponenteAgregado);

        String siguienteVista = determinarSiguienteVista(tipoComponente, armadoPcDtoConComponenteAgregado);

        ComponenteDto componenteAgregado = this.servicioArmaTuPc.obtenerComponenteDtoPorId(idComponente);

        model.put("agregado", "x"+cantidad +" "+ componenteAgregado.getModelo() + " agregado correctamente al armado!");

        return new ModelAndView("redirect:/arma-tu-pc/tradicional/" + siguienteVista, model);
    }

    public ModelAndView quitarComponenteDelArmado(@PathVariable("tipoComponente")String tipoComponente,
                                                  @RequestParam("id") Long idComponente,
                                                  @RequestParam("cantidad") Integer cantidad,
                                                  HttpSession session){
        ModelMap model = new ModelMap();

        ArmadoPcDto armadoPcDtoConComponenteQuitado = null;

        try {
            armadoPcDtoConComponenteQuitado = this.servicioArmaTuPc.quitarComponenteAlArmado(idComponente, tipoComponente, cantidad, obtenerArmadoPcDtoDeLaSession(session));
        } catch (QuitarComponenteInvalidoException e) {
            model.put("errorQuitado", "No es posible quitar un componente que no fue agregado al armado.");
            return new ModelAndView("redirect:/arma-tu-pc/tradicional/" + tipoComponente, model);
        } catch (QuitarStockDemasDeComponenteException e) {
            model.put("errorQuitado", "No es posible quitar una cantidad del componente que no posee el armado.");
            return new ModelAndView("redirect:/arma-tu-pc/tradicional/" + tipoComponente, model);
        }

        session.setAttribute("armadoPcDto", armadoPcDtoConComponenteQuitado);

        ComponenteDto componenteQuitado = this.servicioArmaTuPc.obtenerComponenteDtoPorId(idComponente);

        model.put("quitado", "x"+cantidad +" "+ componenteQuitado.getModelo() + " fue quitado del armado.");

        return new ModelAndView("redirect:/arma-tu-pc/tradicional/" + tipoComponente, model);
    }

    private String determinarSiguienteVista(String tipoComponente, ArmadoPcDto armadoPcDto) {

        if(this.servicioArmaTuPc.sePuedeAgregarMasUnidades(tipoComponente, armadoPcDto)) return tipoComponente;

        return obtenerPasoSiguiente(tipoComponente);
    }

    @RequestMapping(path = "arma-tu-pc/tradicional/resumen", method = RequestMethod.GET)
    public ModelAndView obtenerResumen(HttpSession session) {
        ModelMap model = new ModelMap();

        ArmadoPcDto armadoPcDto = obtenerArmadoPcDtoDeLaSession(session);

        if(this.servicioArmaTuPc.armadoCompleto(armadoPcDto)) model.put("armadoPcDto", armadoPcDto);
        else model.put("errorResumen", "Seleccione almenos un motherboard, cpu, cooler y gabinete para obtener su armado");

        return new ModelAndView("arma-tu-pc/tradicional/resumen", model);
    }

    @RequestMapping(path = "arma-tu-pc/tradicional/reiniciar-armado", method = RequestMethod.POST)
    public ModelAndView reiniciarArmado(HttpSession session) {
        session.removeAttribute("armadoPcDto");
        return new ModelAndView("redirect:/arma-tu-pc/tradicional/procesador");
    }

    @RequestMapping(path = "arma-tu-pc/carrito", method = RequestMethod.POST)
    public ModelAndView sumarArmadoAlCarrito(HttpSession session) {

        List<ProductoCarritoDto> carritoSesion;

        if (session.getAttribute("carritoSesion") != null) {
            carritoSesion = (List<ProductoCarritoDto>) session.getAttribute("carritoSesion");
        } else {
            carritoSesion = new ArrayList<>();
        }

        carritoSesion.addAll(this.servicioArmaTuPc.pasajeAProductoDtoParaAgregarAlCarrito(obtenerArmadoPcDtoDeLaSession(session)));

        // controlar el stock con servicio ServicioProductoCarritoImpl

        return new ModelAndView("redirect:/carritoDeCompras/index");
    }

}
