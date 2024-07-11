package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.calendario.ItemRendimiento;
import com.tallerwebi.dominio.calendario.ServicioCalendario;
import com.tallerwebi.dominio.excepcion.ItemRendimientoDuplicadoException;
import com.tallerwebi.dominio.usuario.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;

@Controller
public class ControladorCalendario {

    private final ServicioCalendario servicioCalendario;

    @Autowired
    public ControladorCalendario(ServicioCalendario servicioCalendario) {
        this.servicioCalendario = servicioCalendario;
    }

    @RequestMapping(path = "/calendario", method = RequestMethod.GET)
    public ModelAndView mostrarCalendario(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }

        ModelAndView model = new ModelAndView("verProgreso");
        model.addObject("message", "¿Cómo fue tu entrenamiento hoy?");
        ItemRendimiento itemRendimiento = new ItemRendimiento();
        model.addObject("itemRendimiento", itemRendimiento);
        model.addObject("usuario", usuario);
        return model;
    }

    @RequestMapping(path = "/calendario", method = RequestMethod.POST)
    public ModelAndView guardarItemRendimiento(@ModelAttribute("itemRendimiento") ItemRendimiento itemRendimiento, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }

        ModelAndView model = new ModelAndView("calendario");
        try {
            servicioCalendario.guardarItemRendimientoEnUsuario(itemRendimiento,usuario);
            return new ModelAndView("redirect:/verProgreso");
        } catch (Exception e) {
            model.addObject("error", e.getMessage());
            model.addObject("usuario", usuario);
            return model;
        }
    }

    @RequestMapping(path = "/verProgreso", method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView verProgreso(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }

        ModelAndView model = new ModelAndView("verProgreso");
        List<DatosItemRendimiento> itemsRendimiento = servicioCalendario.getItemsRendimientoDeUsuario(usuario);
        if (itemsRendimiento.isEmpty()) {
            model.addObject("mensaje", "¿Cómo fue tu entrenamiento hoy?");
            model.addObject("sinRendimiento", true); // Atributo para indicar que no hay items de rendimiento
        } else {
            model.addObject("datosItemRendimiento", itemsRendimiento);
            model.addObject("sinRendimiento", false); // Atributo para indicar que sí hay items de rendimiento
        }
        model.addObject("usuario", usuario); // Agregar el usuario al modelo
        return model;
    }



}
