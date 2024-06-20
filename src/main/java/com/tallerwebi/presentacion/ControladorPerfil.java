package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.perfil.Perfil;
import com.tallerwebi.dominio.perfil.ServicioPerfil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
public class ControladorPerfil{

    private final ServicioPerfil servicioPerfil;

    @Autowired
    public ControladorPerfil(ServicioPerfil servicioPerfil) {
        this.servicioPerfil = servicioPerfil;
    }

    @RequestMapping(path = "/perfil", method = RequestMethod.GET)
    public ModelAndView irPerfil(@RequestParam(required = false) Long idPerfil, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView("perfil");
        Perfil perfil = null;

        // Verificar si se ha proporcionado un ID de perfil
        if (idPerfil != null) {
            perfil = servicioPerfil.obtenerPerfilPorId(idPerfil);
        }

        // Si el perfil es null, inicializar un nuevo objeto Perfil
        if (perfil == null) {
            perfil = new Perfil();
        }

        // Agregar el perfil al modelo
        modelAndView.addObject("perfil", perfil);
        session.setAttribute("perfil", perfil);

        return modelAndView;
    }

    @RequestMapping(path = "/guardar", method = RequestMethod.POST)
    public String guardarPerfil(@ModelAttribute Perfil perfil, HttpSession session) {

    }

    @RequestMapping(path = "/perfil/actualizar", method = RequestMethod.POST)
    public String actualizarPerfil(@RequestParam Long idPerfil, @ModelAttribute Perfil perfil) {
        servicioPerfil.actualizarPerfil(idPerfil, perfil);
        return "redirect:/perfil?idPerfil=" + idPerfil;
    }

}
