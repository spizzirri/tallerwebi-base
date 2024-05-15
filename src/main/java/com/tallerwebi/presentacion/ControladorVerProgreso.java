package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.calendario.ItemRendimiento;
import com.tallerwebi.dominio.calendario.ServicioCalendario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
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

    @RequestMapping(path = "/verProgreso", method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView irVerProgreso() {
        List<DatosItemRendimiento> datosItemRendimiento = servicioCalendario.obtenerItemsRendimiento();
        ModelMap model = new ModelMap();
        model.put("datosItemRendimiento", datosItemRendimiento);
        return new ModelAndView("verProgreso", model);
    }


}
