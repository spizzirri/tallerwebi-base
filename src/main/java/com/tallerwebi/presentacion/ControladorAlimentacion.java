package com.tallerwebi.presentacion;
import com.tallerwebi.dominio.alimentacion.ServicioAlimentacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorAlimentacion{

    private final ServicioAlimentacion servicioAlimentacion;

    @Autowired
    public ControladorAlimentacion(ServicioAlimentacion servicioAlimentacion) {
        this.servicioAlimentacion = servicioAlimentacion;
    }

    @RequestMapping(path = "/alimentacion", method = RequestMethod.GET)
    public ModelAndView irAlimentacion() {
        String viewName = "alimentacion";
        ModelMap model = new ModelMap();
        return new ModelAndView(viewName, model);
    }

}
