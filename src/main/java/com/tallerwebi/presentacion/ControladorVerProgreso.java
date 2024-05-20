package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.calendario.ItemRendimiento;
import com.tallerwebi.dominio.calendario.ServicioCalendario;
import com.tallerwebi.dominio.calendario.TipoRendimiento;
import org.dom4j.rule.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class ControladorVerProgreso {

    @Autowired
    private ServicioCalendario servicioCalendario;

    @Autowired
    public ControladorVerProgreso(ServicioCalendario servicioCalendario) {
        this.servicioCalendario = servicioCalendario;
    }

}
