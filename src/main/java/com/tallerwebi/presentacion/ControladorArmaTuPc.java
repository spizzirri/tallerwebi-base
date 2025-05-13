package com.tallerwebi.presentacion;

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

    List<ComponenteDto> procesadores;
    List<ComponenteDto> motherboards;
    List<ComponenteDto> coolers;

    public ControladorArmaTuPc() {
        // Lista quemada porque todavia no vimos capa de servicio (determinar despues si es un treeset)
        this.procesadores = Arrays.asList(
                new ComponenteDto(1L,"Procesador","Procesador1", 1000D),
                new ComponenteDto(2L,"Procesador","Procesador2", 2000D),
                new ComponenteDto(3L,"Procesador","Procesador3", 3000D)
        );
        this.motherboards = Arrays.asList(
                new ComponenteDto(1L,"Motherboard","Motherboard1", 1000D),
                new ComponenteDto(2L,"Motherboard","Motherboard2", 2000D),
                new ComponenteDto(3L,"Motherboard","Motherboard3", 3000D)
        );
        this.coolers = Arrays.asList(
                new ComponenteDto(1L,"Cooler","Cooler1", 1000D),
                new ComponenteDto(2L,"Cooler","Cooler2", 2000D),
                new ComponenteDto(3L,"Cooler","Cooler3", 3000D)
        );
    }

     private ArmadoPcDto obtenerArmadoPcDtoCorrespondiente(HttpSession session) {
        if(session.getAttribute("armadoPcDto") == null){ // si no existe el armado de pc en la session, se la creo
            ArmadoPcDto armadoPcDto = new ArmadoPcDto();
            session.setAttribute("armadoPcDto", armadoPcDto);
        }// si ya existia entonces lo rescatamos de la sesion
        return (ArmadoPcDto) session.getAttribute("armadoPcDto");
    }

     private ComponenteDto obtenerComponenteDtoDeUnaLista(Long id, List<ComponenteDto> listaDeComponentes){// nose si esto se va a usar una vez visto servicios
        return listaDeComponentes.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst() // devolvelo si lo encontras
                .orElse(null); // caso contrario devolveme un null (si no aclaro esto lanza excepcion)
    }


    @RequestMapping(path="/arma-tu-pc/index", method= RequestMethod.GET)
    public ModelAndView armarUnaPc() {

        ModelMap model = new ModelMap();

        model.put("armadoPcDto", new ArmadoPcDto());

        model.put("procesadores", this.procesadores);

        return new ModelAndView("arma-tu-pc/index", model);
    }

    @RequestMapping(path="/arma-tu-pc/index", method= RequestMethod.POST)
    public ModelAndView seleccionarProcesador(@RequestParam("id")Long idProcesador, HttpSession session) {
        ArmadoPcDto armadoPcDto = obtenerArmadoPcDtoCorrespondiente(session);

        ComponenteDto procesadorSeleccionado = obtenerComponenteDtoDeUnaLista(idProcesador, this.procesadores);

        armadoPcDto.setProcesador(procesadorSeleccionado);
        return new ModelAndView("redirect:/arma-tu-pc/motherboard");
    }

    @RequestMapping(path = "/arma-tu-pc/motherboard")
    public ModelAndView cargarMotherboards(HttpSession session) {
        ArmadoPcDto armadoPcDto = obtenerArmadoPcDtoCorrespondiente(session);

        ModelMap model = new ModelMap();
        model.put("armadoPcDto", armadoPcDto);

        model.put("motherboards", this.motherboards);

        return new ModelAndView("arma-tu-pc/motherboard", model);
    }

    @RequestMapping(path="/arma-tu-pc/motherboard" , method= RequestMethod.POST)
    public ModelAndView seleccionarMotherboard(@RequestParam("id") Long idMother, HttpSession session) {
        ArmadoPcDto armadoPcDto = obtenerArmadoPcDtoCorrespondiente(session);

        ComponenteDto motherSeleccionado = obtenerComponenteDtoDeUnaLista(idMother,this.motherboards);

        armadoPcDto.setMotherboard(motherSeleccionado);

        return new ModelAndView("redirect:/arma-tu-pc/cooler");
    }

    @RequestMapping(path="/arma-tu-pc/cooler", method= RequestMethod.GET)
    public ModelAndView cargarCoolers(HttpSession session) {
        ArmadoPcDto armadoPcDto = obtenerArmadoPcDtoCorrespondiente(session);

        ModelMap model = new ModelMap();
        model.put("armadoPcDto", armadoPcDto);

        model.put("coolers", this.coolers);

        return new ModelAndView("arma-tu-pc/coolers", model);
    }
}
