package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.perfil.Perfil;
import com.tallerwebi.dominio.perfil.ServicioPerfil;
import com.tallerwebi.dominio.usuario.ServicioLogin;
import com.tallerwebi.dominio.usuario.ServicioLoginImpl;
import com.tallerwebi.dominio.usuario.Usuario;
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

    @Autowired
    private ServicioPerfil servicioPerfil;

    @Autowired
    private ServicioLogin servicioLogin;

    @Autowired
    public ControladorPerfil(ServicioPerfil servicioPerfil, ServicioLogin servicioLogin) {
        this.servicioPerfil = servicioPerfil;
        this.servicioLogin = servicioLogin;
    }

    @RequestMapping(path = "/perfil", method = RequestMethod.GET)
    public ModelAndView irPerfil(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }

        Perfil perfil = usuario.getPerfil();
        ModelAndView modelAndView = new ModelAndView("perfil");

        // Si no existe perfil, crear uno nuevo
        if (perfil == null) {
            perfil = new Perfil();
        }

        modelAndView.addObject("usuario", usuario);
        modelAndView.addObject("perfil", perfil);

        return modelAndView;
    }

    @RequestMapping(path = "/guardar", method = RequestMethod.POST)
    public ModelAndView guardarPerfil(@ModelAttribute Perfil perfil, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }

        ModelAndView modelAndView = new ModelAndView("redirect:/perfil");
        modelAndView.addObject("usuario", usuario);

        servicioLogin.guardarPerfil(usuario, perfil);

        return modelAndView;
    }

}
