package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.calendario.ItemRendimiento;
import com.tallerwebi.dominio.calendario.ServicioCalendario;
import com.tallerwebi.dominio.calendario.TipoRendimiento;
import com.tallerwebi.dominio.excepcion.ItemRendimientoDuplicadoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.util.List;

@Controller
public class ControladorCalendario {

    private ServicioCalendario servicioCalendario;

    @Autowired
    public ControladorCalendario(ServicioCalendario servicioCalendario) {
        this.servicioCalendario = servicioCalendario;
    }

    @RequestMapping(path = "/calendario", method = RequestMethod.GET)
    public ModelAndView irCalendario() {
        String viewName = "calendario"; // Nombre correcto de la vista
        ModelMap model = new ModelMap();
        // Agregar el primer objeto al modelo
        model.put("message", "¿Cómo fue tu entrenamiento hoy?");
        // Crear un nuevo objeto ItemRendimiento y agregarlo al modelo
        ItemRendimiento itemRendimiento = new ItemRendimiento();
        model.put("itemRendimiento", itemRendimiento);
        // Retornar el ModelAndView con el nombre de la vista y el modelo
        return new ModelAndView(viewName, model);
    }

    @RequestMapping(path = "/calendario", method = RequestMethod.POST)
    public ModelAndView verProgreso(@ModelAttribute("itemRendimiento") ItemRendimiento itemRendimiento) {
        ModelMap model = new ModelMap();
        itemRendimiento.setFecha(LocalDate.now());

        try {
            servicioCalendario.guardarItemRendimiento(itemRendimiento);
            return new ModelAndView("redirect:/verProgreso");
        } catch (ItemRendimientoDuplicadoException e) {
            model.addAttribute("error", e.getMessage());
            return new ModelAndView("calendario", model);
        }
    }


    @RequestMapping(path = "/verProgreso", method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView irVerProgreso() {
        List<DatosItemRendimiento> datosItemRendimiento = servicioCalendario.obtenerItemsRendimiento();
        ModelMap model = new ModelMap();
        model.put("datosItemRendimiento", datosItemRendimiento);
        return new ModelAndView("verProgreso", model);
    }



}
