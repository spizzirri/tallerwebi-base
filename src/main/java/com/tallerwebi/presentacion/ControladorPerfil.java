package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.excepcion.NoCargaPerfilExeption;
import com.tallerwebi.dominio.excepcion.NoPudoGuardarPerfilException;
import com.tallerwebi.dominio.perfil.Perfil;
import com.tallerwebi.dominio.perfil.ServicioPerfil;
import com.tallerwebi.dominio.rutina.ServicioRutina;
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
public class ControladorPerfil {

    @Autowired
    private ServicioPerfil servicioPerfil;

    @Autowired
    private ServicioLogin servicioLogin;

    @Autowired
    private ServicioRutina servicioRutina;

    @Autowired
    public ControladorPerfil(ServicioPerfil servicioPerfil, ServicioLogin servicioLogin, ServicioRutina servicioRutina) {
        this.servicioPerfil = servicioPerfil;
        this.servicioLogin = servicioLogin;
        this.servicioRutina = servicioRutina;
    }

    @RequestMapping(path = "/perfil", method = RequestMethod.GET)
    public ModelAndView irPerfil(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }

        if (usuario.getObjetivo() == null) {
            return new ModelAndView("redirect:/objetivo");
        }


        Perfil perfil = usuario.getPerfil();
        ModelAndView modelAndView = new ModelAndView("perfil");

        try {
            // Si no existe perfil, crear uno nuevo
            if (perfil == null) {
                perfil = new Perfil();
            } else {
                // Generar la recomendación
                String recomendacion = servicioPerfil.generarRecomendacion(perfil);
                perfil.setRecomendacion(recomendacion);
            }

            modelAndView.addObject("usuario", usuario);
            modelAndView.addObject("perfil", perfil);

        } catch (NoCargaPerfilExeption e) {
            modelAndView = new ModelAndView("error");
            modelAndView.addObject("mensaje", "Error al cargar el perfil: " + e.getMessage());
        }

        return modelAndView;
    }

    @RequestMapping(path = "/guardar", method = RequestMethod.POST)
    public ModelAndView guardarPerfil(@ModelAttribute Perfil perfil, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }

        ModelAndView modelAndView = new ModelAndView("redirect:/perfil");

        try {
            // Generar la recomendación
            String recomendacion = servicioPerfil.generarRecomendacion(perfil);
            perfil.setRecomendacion(recomendacion);

            modelAndView.addObject("usuario", usuario);

            servicioLogin.guardarPerfil(usuario, perfil);

        } catch (NoPudoGuardarPerfilException e) {
            modelAndView = new ModelAndView("error");
            modelAndView.addObject("mensaje", "Error al guardar el perfil: " + e.getMessage());
        }

        return modelAndView;
    }

    @RequestMapping(path = "/cerrar-sesion", method = RequestMethod.POST)
    public String cerrarSesion(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

}
