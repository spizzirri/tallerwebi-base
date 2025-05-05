package com.tallerwebi.presentacion;

import com.tallerwebi.presentacion.dto.ArmadoPcDto;
import com.tallerwebi.presentacion.dto.ComponenteDto;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

public class ControladorArmaTuPc {

    public ModelAndView armarUnaPc() {

        ModelMap model = new ModelMap();

        model.put("armadoPcDto", new ArmadoPcDto());

        // Lista quemada porque todavia no vimos capa de servicio (determinar despues si es un treeset)
        List<ComponenteDto> procesadores = new ArrayList<>();
        procesadores.add(new ComponenteDto());
        procesadores.add(new ComponenteDto());
        procesadores.add(new ComponenteDto());

        model.put("procesadores", procesadores);

        return new ModelAndView("arma-tu-pc", model);
    }
}
