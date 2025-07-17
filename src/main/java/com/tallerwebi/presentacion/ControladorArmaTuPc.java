package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioArmaTuPc;
import com.tallerwebi.dominio.ServicioPrecios;
import com.tallerwebi.dominio.entidades.Componente;
import com.tallerwebi.dominio.excepcion.*;
import com.tallerwebi.presentacion.dto.ArmadoPcDto;
import com.tallerwebi.presentacion.dto.ComponenteDto;
import com.tallerwebi.presentacion.dto.ProductoCarritoArmadoDto;
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

    private ArmadoPcDto obtenerArmadoPcDtoCustomDeLaSession(HttpSession session) {
        if(session.getAttribute("armadoPcDtoCustom") == null){ // si no existe el armado de pc en la session, se la creo
            ArmadoPcDto armadoPcDto = new ArmadoPcDto();
            session.setAttribute("armadoPcDtoCustom", armadoPcDto);
        }// si ya existia entonces lo rescatamos de la sesion
        return (ArmadoPcDto) session.getAttribute("armadoPcDtoCustom");
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
                    ? new ArrayList<>(this.servicioArmaTuPc.obtenerListaDeComponentesCompatiblesFiltradosDto(tipoComponente, query, armadoPcDto))
                    : new ArrayList<>(this.servicioArmaTuPc.obtenerListaDeComponentesCompatiblesDto(tipoComponente, armadoPcDto));

            model.put("componentesLista", this.pasarPreciosAPesos(componentesCompatiblesADevolver));

            if (tipoComponente.equals("memoria")) model.put("slotsRamMotherboardElegida", this.servicioArmaTuPc.obtenerSlotsRamDeMotherboard(armadoPcDto.getMotherboard()));
            if (tipoComponente.equals("almacenamiento")){
                model.put("slotsSataMotherboardElegida", this.servicioArmaTuPc.obtenerSlotsSataDeMotherboard(armadoPcDto.getMotherboard()));
                model.put("slotsM2MotherboardElegida", this.servicioArmaTuPc.obtenerSlotsM2DeMotherboard(armadoPcDto.getMotherboard()));
            }

        } catch (ComponenteDeterminateDelArmadoEnNullException e) {
            model.put("errorLista", e.getMessage());
        }

        if ((tipoComponente.equals("procesador") && armadoPcDto.getProcesador() == null)
            || (tipoComponente.equals("motherboard") && armadoPcDto.getMotherboard() == null)
            || (tipoComponente.equals("cooler") && armadoPcDto.getCooler() == null)
            || (tipoComponente.equals("gabinete") && armadoPcDto.getGabinete() == null)) model.put("componenteEscencialFaltante", "Este componente es obligatorio para completar el armado.");

        Integer wattsDeArmado = this.servicioArmaTuPc.obtenerWattsTotalesDeArmado(armadoPcDto);

        model.put("wattsArmado", wattsDeArmado);
        model.put("armadoPcDto", armadoPcDto);
        model.put("idsDeComponentesSeleccionados", obtenerIdsDeArmadoDeSession(armadoPcDto));

        //falta determinar si ingresa a un paso que no existe

        model.put("pasoActual", tipoComponente);
        model.put("pasoAnterior", obtenerPasoAnterior(tipoComponente));
        model.put("pasoSiguiente", obtenerPasoSiguiente(tipoComponente));

        return new ModelAndView("arma-tu-pc/tradicional/" + tipoComponente, model);
    }

    @RequestMapping(path = "/arma-tu-pc/custom/{tipoComponente}", method = RequestMethod.GET)
    public ModelAndView cargarComponenteArmadoCustom(@PathVariable("tipoComponente") String tipoComponente,
                                                        @RequestParam(name = "appSeleccionada[]", required = false) List<String> seleccionados,
                                                        @RequestParam(name = "selectorRequisitos", required = false) String selectorRequisitos,
                                                        @RequestParam(value = "q", required = false) String query,
                                                        HttpSession sesion) {
        ModelMap model = new ModelMap();
        ArmadoPcDto armadoPcDto = obtenerArmadoPcDtoCustomDeLaSession(sesion);

        String selectorRequisitosSesion;
        List<String> seleccionadosSesion;

        if (sesion.getAttribute("selectorRequisitos") == null) {
            sesion.setAttribute("selectorRequisitos", selectorRequisitos);
            selectorRequisitosSesion = (String) sesion.getAttribute("selectorRequisitos");
        } else {
            selectorRequisitosSesion = (String) sesion.getAttribute("selectorRequisitos");
        }

        if (sesion.getAttribute("appSeleccionada") == null) {
            sesion.setAttribute("appSeleccionada", seleccionados);
            seleccionadosSesion = (List<String>) sesion.getAttribute("appSeleccionada");
        } else {
            seleccionadosSesion = (List<String>) sesion.getAttribute("appSeleccionada");
        }

        try {
            List<ComponenteDto> componentesCompatiblesADevolver;

            if (seleccionadosSesion != null) {
                if (selectorRequisitosSesion.equals("requisitosMinimos")){
                    model.put("mapaRequisitos", this.servicioArmaTuPc.obtenerMapaDeRequisitosMinimosSeleccionados(seleccionadosSesion));
                    componentesCompatiblesADevolver = (query != null)
                            ? this.servicioArmaTuPc.obtenerListaDeComponentesCompatiblesFiltradosDtoCustomRequisitosMinimos(tipoComponente, query, armadoPcDto, this.servicioArmaTuPc.obtenerMapaDeRequisitosMinimosSeleccionados(seleccionadosSesion))
                            : this.servicioArmaTuPc.obtenerListaDeComponentesCompatiblesDtoCustomRequisitosMinimos(tipoComponente, armadoPcDto, this.servicioArmaTuPc.obtenerMapaDeRequisitosMinimosSeleccionados(seleccionadosSesion));
                } else {
                    model.put("mapaRequisitos", this.servicioArmaTuPc.obtenerMapaDeRequisitosRecomendadosSeleccionados(seleccionadosSesion));
                    componentesCompatiblesADevolver = (query != null)
                            ? this.servicioArmaTuPc.obtenerListaDeComponentesCompatiblesFiltradosDtoCustomRequisitosRecomendados(tipoComponente, query, armadoPcDto, this.servicioArmaTuPc.obtenerMapaDeRequisitosRecomendadosSeleccionados(seleccionadosSesion))
                            : this.servicioArmaTuPc.obtenerListaDeComponentesCompatiblesDtoCustomRequisitosRecomendados(tipoComponente, armadoPcDto, this.servicioArmaTuPc.obtenerMapaDeRequisitosRecomendadosSeleccionados(seleccionadosSesion));
                }
            } else {
                return new ModelAndView("redirect:/index");
            }

            model.put("componentesLista", this.pasarPreciosAPesos(componentesCompatiblesADevolver));

            if (tipoComponente.equals("memoria")) model.put("slotsRamMotherboardElegida", this.servicioArmaTuPc.obtenerSlotsRamDeMotherboard(armadoPcDto.getMotherboard()));
            if (tipoComponente.equals("almacenamiento")) {
                model.put("slotsSataMotherboardElegida", this.servicioArmaTuPc.obtenerSlotsSataDeMotherboard(armadoPcDto.getMotherboard()));
                model.put("slotsM2MotherboardElegida", this.servicioArmaTuPc.obtenerSlotsM2DeMotherboard(armadoPcDto.getMotherboard()));
            }

        } catch (ComponenteDeterminateDelArmadoEnNullException e) {
            model.put("errorLista", e.getMessage());
        }

        if ((tipoComponente.equals("procesador") && armadoPcDto.getProcesador() == null)
                || (tipoComponente.equals("motherboard") && armadoPcDto.getMotherboard() == null)
                || (tipoComponente.equals("cooler") && armadoPcDto.getCooler() == null)
                || (tipoComponente.equals("gabinete") && armadoPcDto.getGabinete() == null)) model.put("componenteEscencialFaltante", "Este componente es obligatorio para completar el armado.");

        Integer wattsDeArmado = this.servicioArmaTuPc.obtenerWattsTotalesDeArmado(armadoPcDto);

        model.put("wattsArmado", wattsDeArmado);

        model.put("armadoPcDto", armadoPcDto);
        model.put("idsDeComponentesSeleccionados", obtenerIdsDeArmadoDeSession(armadoPcDto));

        //falta determinar si ingresa a un paso que no existe

        model.put("pasoActual", tipoComponente);
        model.put("pasoAnterior", obtenerPasoAnterior(tipoComponente));
        model.put("pasoSiguiente", obtenerPasoSiguiente(tipoComponente));

        return new ModelAndView("arma-tu-pc/custom/" + tipoComponente, model);
    }

    @RequestMapping(path = "arma-tu-pc/custom/{tipoComponente}/accionCustom", method = RequestMethod.POST)
    public ModelAndView procesarAccionCustom(@PathVariable("tipoComponente")String tipoComponente,
                                       @RequestParam("accionCustom")String accionCustom,
                                       @RequestParam("id")Long idComponente,
                                       @RequestParam("cantidad")Integer cantidad,
                                       HttpSession session){

        switch(accionCustom.toLowerCase()){
            case "agregar":
                return this.agregarComponenteAlArmadoCustom(tipoComponente, idComponente, cantidad, session);
            case "quitar":
                return this.quitarComponenteDelArmadoCustom(tipoComponente, idComponente, cantidad, session);
        }

        ModelMap model = new ModelMap();
        model.put("accionInvalida", "Ingreso una accion invalida.");

        return new ModelAndView("redirect:/arma-tu-pc/custom/" + tipoComponente, model);
    }

    public ModelAndView agregarComponenteAlArmadoCustom(@PathVariable("tipoComponente")String tipoComponente,
                                                  @RequestParam("id") Long idComponente,
                                                  @RequestParam("cantidad") Integer cantidad,
                                                  HttpSession session) {
        ModelMap model = new ModelMap();
        ArmadoPcDto armadoPcDtoConComponenteAgregado = null;
        try {
            armadoPcDtoConComponenteAgregado = this.servicioArmaTuPc.agregarComponenteAlArmado(idComponente, tipoComponente, cantidad, obtenerArmadoPcDtoCustomDeLaSession(session));
        }catch (ComponenteSinStockPedidoException e) {
            model.put("errorLimite", e.getMessage());
            return new ModelAndView("redirect:/arma-tu-pc/custom/" + tipoComponente, model);
        } catch (LimiteDeComponenteSobrepasadoEnElArmadoException e) {
            model.put("errorLimite", "Supero el limite de "+tipoComponente+" de este tipo en su armado");
            return new ModelAndView("redirect:/arma-tu-pc/custom/" + tipoComponente, model);
        }

        armadoPcDtoConComponenteAgregado.setPrecioFormateado(
                this.servicioPrecios.conversionDolarAPeso(armadoPcDtoConComponenteAgregado.getPrecioTotal())
        );

        session.setAttribute("reservaArmadoCustom", this.servicioArmaTuPc.pasajeAProductoArmadoDtoParaAgregarAlCarrito(armadoPcDtoConComponenteAgregado));

        session.setAttribute("armadoPcDtoCustom", armadoPcDtoConComponenteAgregado);

        String siguienteVista = determinarSiguienteVista(tipoComponente, armadoPcDtoConComponenteAgregado);

        ComponenteDto componenteAgregado = this.servicioArmaTuPc.obtenerComponenteDtoPorId(idComponente);

        model.put("agregado", "x"+cantidad +" "+ componenteAgregado.getModelo() + " agregado correctamente al armado!");

        return new ModelAndView("redirect:/arma-tu-pc/custom/" + siguienteVista, model);
    }

    public ModelAndView quitarComponenteDelArmadoCustom(@PathVariable("tipoComponente")String tipoComponente,
                                                  @RequestParam("id") Long idComponente,
                                                  @RequestParam("cantidad") Integer cantidad,
                                                  HttpSession session){
        ModelMap model = new ModelMap();

        ArmadoPcDto armadoPcDtoConComponenteQuitado = null;

        try {
            armadoPcDtoConComponenteQuitado = this.servicioArmaTuPc.quitarComponenteAlArmado(idComponente, tipoComponente, cantidad, obtenerArmadoPcDtoCustomDeLaSession(session));
        } catch (QuitarComponenteInvalidoException e) {
            model.put("errorQuitado", "No es posible quitar un componente que no fue agregado al armado.");
            return new ModelAndView("redirect:/arma-tu-pc/custom/" + tipoComponente, model);
        } catch (QuitarStockDemasDeComponenteException e) {
            model.put("errorQuitado", "No es posible quitar una cantidad del componente que no posee el armado.");
            return new ModelAndView("redirect:/arma-tu-pc/custom/" + tipoComponente, model);
        }

        session.setAttribute("reservaArmadoCustom", this.servicioArmaTuPc.pasajeAProductoArmadoDtoParaAgregarAlCarrito(armadoPcDtoConComponenteQuitado));

        session.setAttribute("armadoPcDtoCustom", armadoPcDtoConComponenteQuitado);

        ComponenteDto componenteQuitado = this.servicioArmaTuPc.obtenerComponenteDtoPorId(idComponente);

        model.put("quitado", "x"+cantidad +" "+ componenteQuitado.getModelo() + " fue quitado del armado.");

        return new ModelAndView("redirect:/arma-tu-pc/custom/" + tipoComponente, model);
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
        }catch (ComponenteSinStockPedidoException e) {
            model.put("errorLimite", e.getMessage());
            return new ModelAndView("redirect:/arma-tu-pc/tradicional/" + tipoComponente, model);
        } catch (LimiteDeComponenteSobrepasadoEnElArmadoException e) {
            model.put("errorLimite", "Supero el limite de "+tipoComponente+" de este tipo en su armado");
            return new ModelAndView("redirect:/arma-tu-pc/tradicional/" + tipoComponente, model);
        }

        armadoPcDtoConComponenteAgregado.setPrecioFormateado(
                this.servicioPrecios.conversionDolarAPeso(armadoPcDtoConComponenteAgregado.getPrecioTotal())
                );

        session.setAttribute("reservaArmado", this.servicioArmaTuPc.pasajeAProductoArmadoDtoParaAgregarAlCarrito(armadoPcDtoConComponenteAgregado));

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

        session.setAttribute("reservaArmado", this.servicioArmaTuPc.pasajeAProductoArmadoDtoParaAgregarAlCarrito(armadoPcDtoConComponenteQuitado));

        session.setAttribute("armadoPcDto", armadoPcDtoConComponenteQuitado);

        ComponenteDto componenteQuitado = this.servicioArmaTuPc.obtenerComponenteDtoPorId(idComponente);

        model.put("quitado", "x"+cantidad +" "+ componenteQuitado.getModelo() + " fue quitado del armado.");

        return new ModelAndView("redirect:/arma-tu-pc/tradicional/" + tipoComponente, model);
    }

    private String determinarSiguienteVista(String tipoComponente, ArmadoPcDto armadoPcDto) {

        if(this.servicioArmaTuPc.sePuedeAgregarMasUnidades(tipoComponente, armadoPcDto)) return tipoComponente;

        return obtenerPasoSiguiente(tipoComponente);
    }

    @RequestMapping(path = "arma-tu-pc/custom/resumen", method = RequestMethod.GET)
    public ModelAndView obtenerResumenCustom(HttpSession session) {
        ModelMap model = new ModelMap();

        ArmadoPcDto armadoPcDto = obtenerArmadoPcDtoCustomDeLaSession(session);

        if(this.servicioArmaTuPc.armadoCompleto(armadoPcDto)) model.put("armadoPcDto", armadoPcDto);
        else model.put("errorResumen", "Seleccione almenos un motherboard, cpu, cooler y gabinete para obtener su armado");

        return new ModelAndView("arma-tu-pc/custom/resumen", model);
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

        this.servicioArmaTuPc.devolverStockDeArmado(obtenerArmadoPcDtoDeLaSession(session));

        session.removeAttribute("armadoPcDto");
        return new ModelAndView("redirect:/arma-tu-pc/tradicional/procesador");
    }

    @RequestMapping(path = "arma-tu-pc/custom/reiniciar-armado", method = RequestMethod.POST)
    public ModelAndView reiniciarArmadoCustom(HttpSession session) {

        this.servicioArmaTuPc.devolverStockDeArmado(obtenerArmadoPcDtoCustomDeLaSession(session));

        session.removeAttribute("armadoPcDtoCustom");
        return new ModelAndView("redirect:/arma-tu-pc/custom/procesador");
    }

    @RequestMapping(path = "arma-tu-pc/carrito", method = RequestMethod.POST)
    public ModelAndView sumarArmadoAlCarrito(HttpSession session) {

        List<ProductoCarritoDto> carritoSesion;

        if (session.getAttribute("carritoSesion") != null) {
            carritoSesion = (List<ProductoCarritoDto>) session.getAttribute("carritoSesion");
        } else {
            carritoSesion = new ArrayList<>();
        }

        Integer numeroDeUltimoArmadoEnElCarrito = obtenerElNumeroDelUltimoArmadoDelCarritoDeSesion(carritoSesion);

        List<ProductoCarritoArmadoDto> productoCarritoArmadoDtos = this.obtenerReservaArmadoDeLaSession(session);

        productoCarritoArmadoDtos = this.servicioArmaTuPc.configurarNumeroDeArmadoYEscencialidadAProductosCarritoArmadoDto(numeroDeUltimoArmadoEnElCarrito, productoCarritoArmadoDtos);

        carritoSesion.addAll(productoCarritoArmadoDtos);

        session.removeAttribute("armadoPcDto");
        session.removeAttribute("reservaArmado");

        return new ModelAndView("redirect:/carritoDeCompras/index");
    }

    @RequestMapping(path = "arma-tu-pc/custom/carrito", method = RequestMethod.POST)
    public ModelAndView sumarArmadoCustomAlCarrito(HttpSession session) {

        List<ProductoCarritoDto> carritoSesion;

        if (session.getAttribute("carritoSesion") != null) {
            carritoSesion = (List<ProductoCarritoDto>) session.getAttribute("carritoSesion");
        } else {
            carritoSesion = new ArrayList<>();
        }

        Integer numeroDeUltimoArmadoEnElCarrito = obtenerElNumeroDelUltimoArmadoDelCarritoDeSesion(carritoSesion);

        List<ProductoCarritoArmadoDto> productoCarritoArmadoDtos = this.obtenerReservaArmadoCustomDeLaSession(session);

        productoCarritoArmadoDtos = this.servicioArmaTuPc.configurarNumeroDeArmadoYEscencialidadAProductosCarritoArmadoDto(numeroDeUltimoArmadoEnElCarrito, productoCarritoArmadoDtos);

        carritoSesion.addAll(productoCarritoArmadoDtos);

        session.removeAttribute("armadoPcDtoCustom");
        session.removeAttribute("reservaArmadoCustom");

        return new ModelAndView("redirect:/carritoDeCompras/index");
    }

    private List<ProductoCarritoArmadoDto> obtenerReservaArmadoDeLaSession(HttpSession session) {

        return (session.getAttribute("reservaArmado") != null)
                ? (List<ProductoCarritoArmadoDto>)session.getAttribute("reservaArmado")
                : new ArrayList<>();

    }

    private List<ProductoCarritoArmadoDto> obtenerReservaArmadoCustomDeLaSession(HttpSession session) {

        return (session.getAttribute("reservaArmadoCustom") != null)
                ? (List<ProductoCarritoArmadoDto>)session.getAttribute("reservaArmadoCustom")
                : new ArrayList<>();

    }

    private Integer obtenerElNumeroDelUltimoArmadoDelCarritoDeSesion(List<ProductoCarritoDto> carritoSesion) {

        Integer numeroMasAlto = 0;

        for (ProductoCarritoDto producto : carritoSesion) {
            if (producto instanceof ProductoCarritoArmadoDto){
                int numeroDeProductoActual = ((ProductoCarritoArmadoDto)producto).getNumeroDeArmadoAlQuePertenece();
                if (numeroDeProductoActual > numeroMasAlto) numeroMasAlto = numeroDeProductoActual;
            }
        }

        return numeroMasAlto;

    }

    @GetMapping("/arma-tu-pc/armaTuPc")
    public String elegirTipoDeArmado() {
        return "arma-tu-pc/armaTuPc";
    }

    @GetMapping("/arma-tu-pc/armadoCustom")
    public String seleccionCustom(HttpSession sesion) {

        this.servicioArmaTuPc.devolverStockDeArmado(obtenerArmadoPcDtoCustomDeLaSession(sesion));
        sesion.removeAttribute("armadoPcDtoCustom");

        sesion.removeAttribute("selectorRequisitos");
        sesion.removeAttribute("appSeleccionada");
        return "arma-tu-pc/armadoCustom";
    }

}
