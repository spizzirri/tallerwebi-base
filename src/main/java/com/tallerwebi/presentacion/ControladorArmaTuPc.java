package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioArmaTuPc;
import com.tallerwebi.presentacion.dto.ArmadoPcDto;
import com.tallerwebi.presentacion.dto.ComponenteDto;
import org.dom4j.rule.Mode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@Controller
public class ControladorArmaTuPc {

    private ServicioArmaTuPc servicioArmaTuPc;
    // esto se podria devolver con un servicio mockeado en el test
    List<ComponenteDto> procesadores;
    List<ComponenteDto> motherboards;
    List<ComponenteDto> coolers;
    List<ComponenteDto> memorias;
    List<ComponenteDto> gpus;
    List<ComponenteDto> almacenamientos;
    List<ComponenteDto> fuentes;
    List<ComponenteDto> gabinetes;
    List<ComponenteDto> monitores;
    List<ComponenteDto> perifericos;

    public ControladorArmaTuPc(ServicioArmaTuPc servicioArmaTuPc) { this.servicioArmaTuPc =  servicioArmaTuPc; }
    public ControladorArmaTuPc() {
        // Lista quemada porque todavia no vimos capa de servicio (determinar despues si es un treeset)
//        this.procesadores = Arrays.asList(
//                new ComponenteDto(1L,"Procesador","Procesador1", 1000D),
//                new ComponenteDto(2L,"Procesador","Procesador2", 2000D),
//                new ComponenteDto(3L,"Procesador","Procesador3", 3000D)
//        );
//        this.motherboards = Arrays.asList(
//                new ComponenteDto(1L,"Motherboard","Motherboard1", 1000D),
//                new ComponenteDto(2L,"Motherboard","Motherboard2", 2000D),
//                new ComponenteDto(3L,"Motherboard","Motherboard3", 3000D)
//        );
//        this.coolers = Arrays.asList(
//                new ComponenteDto(1L,"Cooler","Cooler1", 1000D),
//                new ComponenteDto(2L,"Cooler","Cooler2", 2000D),
//                new ComponenteDto(3L,"Cooler","Cooler3", 3000D)
//        );
//        this.memorias = Arrays.asList(
//                new ComponenteDto(1L,"Memoria","Memoria1", 1000D),
//                new ComponenteDto(2L,"Memoria","Memoria2", 2000D),
//                new ComponenteDto(3L,"Memoria","Memoria3", 3000D)
//        );
        this.gpus = Arrays.asList(
                new ComponenteDto(1L,"Gpu","Gpu1", 1000D),
                new ComponenteDto(2L,"Gpu","Gpu2", 2000D),
                new ComponenteDto(3L,"Gpu","Gpu3", 3000D)
        );
        this.almacenamientos = Arrays.asList(
                new ComponenteDto(1L,"Almacenamiento","Almacenamiento1", 1000D),
                new ComponenteDto(2L,"Almacenamiento","Almacenamiento2", 2000D),
                new ComponenteDto(3L,"Almacenamiento","Almacenamiento3", 3000D)
        );
        this.fuentes = Arrays.asList(
                new ComponenteDto(1L,"Fuente","Fuente1", 1000D),
                new ComponenteDto(2L,"Fuente","Fuente2", 2000D),
                new ComponenteDto(3L,"Fuente","Fuente3", 3000D)
        );
        this.gabinetes = Arrays.asList(
                new ComponenteDto(1L,"Gabinete","Gabinete1", 1000D),
                new ComponenteDto(2L,"Gabinete","Gabinete2", 2000D),
                new ComponenteDto(3L,"Gabinete","Gabinete3", 3000D)
        );
        this.monitores = Arrays.asList(
                new ComponenteDto(1L,"Monitor","Monitor1", 1000D),
                new ComponenteDto(2L,"Monitor","Monitor2", 2000D),
                new ComponenteDto(3L,"Monitor","Monitor3", 3000D)
        );
        this.perifericos = Arrays.asList(
                new ComponenteDto(1L,"Periferico","Periferico1", 1000D),
                new ComponenteDto(2L,"Periferico","Periferico2", 2000D),
                new ComponenteDto(3L,"Periferico","Periferico3", 3000D)
        );
    }

     private ArmadoPcDto obtenerArmadoPcDtoCorrespondiente(HttpSession session) {
        if(session.getAttribute("armadoPcDto") == null){ // si no existe el armado de pc en la session, se la creo
            ArmadoPcDto armadoPcDto = new ArmadoPcDto();
            session.setAttribute("armadoPcDto", armadoPcDto);
        }// si ya existia entonces lo rescatamos de la sesion
        return (ArmadoPcDto) session.getAttribute("armadoPcDto");
    }

//     private ComponenteDto obtenerComponenteDtoDeUnaLista(Long id, List<ComponenteDto> listaDeComponentes){// nose si esto se va a usar una vez visto servicios
//        return listaDeComponentes.stream()
//                .filter(p -> p.getId().equals(id))
//                .findFirst() // devolvelo si lo encontras
//                .orElse(null); // caso contrario devolveme un null (si no aclaro esto lanza excepcion)
//    }


    @RequestMapping(path="/arma-tu-pc/index", method= RequestMethod.GET)
    public ModelAndView armarUnaPc() {

        ModelMap model = new ModelMap();

        model.put("armadoPcDto", new ArmadoPcDto());

        model.put("procesadores", this.servicioArmaTuPc.obtenerListaDeComponentesDto("Procesador"));

        return new ModelAndView("arma-tu-pc/index", model);
    }

    @RequestMapping(path="/arma-tu-pc/index", method= RequestMethod.POST)
    public ModelAndView seleccionarProcesador(@RequestParam("id")Long idProcesador, HttpSession session) {
        ArmadoPcDto armadoPcDto = obtenerArmadoPcDtoCorrespondiente(session);

        ComponenteDto procesadorSeleccionado = this.servicioArmaTuPc.obtenerComponenteDtoPorId(idProcesador);

        armadoPcDto.setProcesador(procesadorSeleccionado);
        return new ModelAndView("redirect:/arma-tu-pc/motherboard");
    }

    @RequestMapping(path = "/arma-tu-pc/motherboard")
    public ModelAndView cargarMotherboards(HttpSession session) {

        ModelMap model = new ModelMap();
        model.put("armadoPcDto", obtenerArmadoPcDtoCorrespondiente(session));

        model.put("motherboards", this.servicioArmaTuPc.obtenerListaDeComponentesDto("Motherboard"));

        return new ModelAndView("arma-tu-pc/motherboard", model);
    }

    @RequestMapping(path="/arma-tu-pc/motherboard" , method= RequestMethod.POST)
    public ModelAndView seleccionarMotherboard(@RequestParam("id") Long idMother, HttpSession session) {
        ArmadoPcDto armadoPcDto = obtenerArmadoPcDtoCorrespondiente(session);

        ComponenteDto motherSeleccionado = this.servicioArmaTuPc.obtenerComponenteDtoPorId(idMother);

        armadoPcDto.setMotherboard(motherSeleccionado);

        return new ModelAndView("redirect:/arma-tu-pc/cooler");
    }

    @RequestMapping(path="/arma-tu-pc/cooler", method= RequestMethod.GET)
    public ModelAndView cargarCoolers(HttpSession session) {

        ModelMap model = new ModelMap();
        model.put("armadoPcDto",  obtenerArmadoPcDtoCorrespondiente(session));

        model.put("coolers", this.servicioArmaTuPc.obtenerListaDeComponentesDto("Cooler"));

        return new ModelAndView("arma-tu-pc/coolers", model);
    }

    @RequestMapping(path="/arma-tu-pc/cooler" , method= RequestMethod.POST)
    public ModelAndView seleccionarCooler(@RequestParam("id") Long idCooler, HttpSession session) {
        ArmadoPcDto armadoPcDto = obtenerArmadoPcDtoCorrespondiente(session);

        ComponenteDto coolerSeleccionado = this.servicioArmaTuPc.obtenerComponenteDtoPorId(idCooler);

        armadoPcDto.setCooler(coolerSeleccionado);

        return new ModelAndView("redirect:/arma-tu-pc/memoria");
    }

    @RequestMapping(path="/arma-tu-pc/memoria", method= RequestMethod.GET)
    public ModelAndView cargarMemorias(HttpSession session) {

        ModelMap model = new ModelMap();
        model.put("armadoPcDto",  obtenerArmadoPcDtoCorrespondiente(session));

        model.put("memorias", this.servicioArmaTuPc.obtenerListaDeComponentesDto("Memoria"));

        return new ModelAndView("arma-tu-pc/memoria", model);
    }

    public ModelAndView seleccionarMemoria(Long idMemoria, Integer cantidad, HttpSession session) {
        ArmadoPcDto armadoPcDto = obtenerArmadoPcDtoCorrespondiente(session);

        ComponenteDto memoriaSeleccionada = this.servicioArmaTuPc.obtenerComponenteDtoPorId(idMemoria);

        ModelMap model = new ModelMap();

        // esta logica de abajo deberia ir en el servicio de armado
        if (armadoPcDto.getRams().size() + cantidad <= 4){ // el limite de rams a cargar la debe establecer la cantidad de sockets de la mother
            for(int i=0 ; i<cantidad; i++)
                armadoPcDto.getRams().add(memoriaSeleccionada);
        } else
            model.put("error", "Supero el limite de memorias");


        String vistaADevolver = null;

        // esta logica de abajo deberia ir en el servicio de armado
        if(armadoPcDto.getRams().size() < 4) vistaADevolver = "arma-tu-pc/memoria"; // el limite de rams a cargar la debe establecer la cantidad de sockets de la mother
        else vistaADevolver = "redirect:/arma-tu-pc/gpu";

        return new ModelAndView(vistaADevolver, model);
    }

    public ModelAndView cargarGpus(HttpSession session) {
        ModelMap model = new ModelMap();
        model.put("armadoPcDto",  obtenerArmadoPcDtoCorrespondiente(session));

        model.put("gpus", this.gpus);

        return new ModelAndView("arma-tu-pc/gpu", model);
    }

    public ModelAndView seleccionarGpu(Long idGpu, HttpSession session) {
        ArmadoPcDto armadoPcDto = obtenerArmadoPcDtoCorrespondiente(session);

        ComponenteDto coolerSeleccionado = this.servicioArmaTuPc.obtenerComponenteDtoPorId(idGpu);

        armadoPcDto.setGpu(coolerSeleccionado);

        return new ModelAndView("redirect:/arma-tu-pc/almacenamiento");
    }

    public ModelAndView cargarAlmacenamientos(HttpSession session) {
        ModelMap model = new ModelMap();
        model.put("armadoPcDto",  obtenerArmadoPcDtoCorrespondiente(session));

        model.put("almacenamientos", this.almacenamientos);

        return new ModelAndView("arma-tu-pc/almacenamiento", model);
    }

    public ModelAndView seleccionarAlmacenamiento(Long idAlmacenamiento, Integer cantidad, HttpSession session) {

        ArmadoPcDto armadoPcDto = obtenerArmadoPcDtoCorrespondiente(session);

        ComponenteDto almacenamientoSeleccionado = this.servicioArmaTuPc.obtenerComponenteDtoPorId(idAlmacenamiento);

        ModelMap model = new ModelMap();

        // esta logica de abajo deberia ir en el servicio de armado
        if (armadoPcDto.getAlmacenamiento().size() + cantidad <= 6) { // el limite de almacenamiento a cargar la debe establecer la cantidad de entradas sata de la mother o la fuente
            for (int i = 0; i < cantidad; i++)
                armadoPcDto.getAlmacenamiento().add(almacenamientoSeleccionado);
        }else
            model.put("error", "Supero el limite de almacenamiento");

        String vistaADevolver = null;

        // esta logica de abajo deberia ir en el servicio de armado
        if(armadoPcDto.getAlmacenamiento().size() < 6) vistaADevolver = "arma-tu-pc/almacenamiento"; // el limite de almacenamiento a cargar la debe establecer la cantidad de entradas sata de la mother o la fuente
        else vistaADevolver = "redirect:/arma-tu-pc/fuente";

        return new ModelAndView(vistaADevolver, model);
    }

    public ModelAndView cargarFuentes(HttpSession session) {
        ModelMap model = new ModelMap();
        model.put("armadoPcDto",  obtenerArmadoPcDtoCorrespondiente(session));
        model.put("fuentes", this.fuentes);
        return new ModelAndView("arma-tu-pc/fuente", model);
    }

    public ModelAndView seleccionarFuente(Long idFuente, HttpSession session) {
        ArmadoPcDto armadoPcDto = obtenerArmadoPcDtoCorrespondiente(session);

        ComponenteDto fuenteSeleccionada = this.servicioArmaTuPc.obtenerComponenteDtoPorId(idFuente);

        armadoPcDto.setFuente(fuenteSeleccionada);

        return new ModelAndView("redirect:/arma-tu-pc/gabinete");
    }

    public ModelAndView cargarGabinetes(HttpSession session) {
        ModelMap model = new ModelMap();
        model.put("armadoPcDto",  obtenerArmadoPcDtoCorrespondiente(session));
        model.put("gabinetes", this.gabinetes);
        return new ModelAndView("arma-tu-pc/gabinete", model);
    }

    public ModelAndView seleccionarGabinete(Long idGabinete, HttpSession session) {
        ArmadoPcDto armadoPcDto = obtenerArmadoPcDtoCorrespondiente(session);

        ComponenteDto gabineteSeleccionado = this.servicioArmaTuPc.obtenerComponenteDtoPorId(idGabinete);

        armadoPcDto.setGabinete(gabineteSeleccionado);

        return new ModelAndView("redirect:/arma-tu-pc/monitor");
    }

    public ModelAndView cargarMonitores(HttpSession session) {
        ModelMap model = new ModelMap();
        model.put("armadoPcDto",  obtenerArmadoPcDtoCorrespondiente(session));
        model.put("monitores", this.monitores);
        return new ModelAndView("arma-tu-pc/monitor", model);
    }

    public ModelAndView seleccionarMonitor(Long idMonitor, HttpSession session) {
        ArmadoPcDto armadoPcDto = obtenerArmadoPcDtoCorrespondiente(session);

        ComponenteDto monitorSeleccionado = this.servicioArmaTuPc.obtenerComponenteDtoPorId(idMonitor);

        armadoPcDto.setMonitor(monitorSeleccionado);

        return new ModelAndView("redirect:/arma-tu-pc/periferico");
    }

    public ModelAndView cargarPerifericos(HttpSession session) {
        ModelMap model = new ModelMap();
        model.put("armadoPcDto",  obtenerArmadoPcDtoCorrespondiente(session));
        model.put("perifericos", this.perifericos);
        return new ModelAndView("arma-tu-pc/periferico", model);
    }

    public ModelAndView seleccionarPeriferico(Long idPeriferico, HttpSession session) {

        ArmadoPcDto armadoPcDto = obtenerArmadoPcDtoCorrespondiente(session);

        ComponenteDto perifericoSeleccionado = this.servicioArmaTuPc.obtenerComponenteDtoPorId(idPeriferico);

        armadoPcDto.getPerifericos().add(perifericoSeleccionado);

        String vistaARetornar = (armadoPcDto.getPerifericos().size() < 10) ? "arma-tu-pc/periferico" : "redirect:arma-tu-pc/resumen";

        return new ModelAndView(vistaARetornar);
    }

    public ModelAndView obtenerResumen(HttpSession session) {
        ModelMap model = new ModelMap();
        ArmadoPcDto armadoPcDtoDeSession = obtenerArmadoPcDtoCorrespondiente(session);


        if (armadoPcDtoDeSession.getProcesador() == null // esto podria pedirselo al servicio
                || armadoPcDtoDeSession.getMotherboard() == null
                || armadoPcDtoDeSession.getCooler() == null
                || armadoPcDtoDeSession.getGabinete() == null) {
            model.put("error", "Seleccione almenos un motherboard, cpu, cooler y gabinete para obtener su armado");
        }else{
            model.put("armadoPcDto", armadoPcDtoDeSession);
        }

        return new ModelAndView("arma-tu-pc/resumen", model);
    }
}
