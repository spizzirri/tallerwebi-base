package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.usuario.ServicioLogin;
import com.tallerwebi.dominio.usuario.Usuario;
import com.tallerwebi.dominio.objetivo.Objetivo;
import com.tallerwebi.dominio.rutina.Rutina;
import com.tallerwebi.dominio.rutina.ServicioRutina;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class ControladorRutina {


    private final ServicioRutina servicioRutina;
    private final ServicioLogin servicioLogin;

    @Autowired
    public ControladorRutina(ServicioRutina servicioRutina, ServicioLogin servicioLogin) {
        this.servicioRutina = servicioRutina;
        this.servicioLogin = servicioLogin;
    }

    @RequestMapping(path = "/rutinas", method = RequestMethod.GET)
    public ModelAndView VerRutinasQueLeInteresanAlUsuario(@RequestParam("objetivo") String objetivo, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }

        ModelAndView modelAndView = new ModelAndView("rutinas");

        Objetivo objetivoEnum;
        try {
            objetivoEnum = Objetivo.valueOf(objetivo);
        } catch (IllegalArgumentException e) {
            modelAndView.addObject("error", "Objetivo no válido");
            return modelAndView;
        }

        List<DatosRutina> rutinas = this.servicioRutina.getRutinasPorObjetivo(objetivoEnum);
        modelAndView.addObject("rutinas", rutinas);
        modelAndView.addObject("objetivoFormateado", objetivoEnum.formatear());
        servicioLogin.guardarObjetivo(usuario, objetivoEnum);
        return modelAndView;
    }

    @RequestMapping(path = "/mi-rutina", method = RequestMethod.GET)
    public ModelAndView irAMiRutina(HttpSession session) {
        ModelAndView modelAndView = new ModelAndView();
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }

        if (usuario.getObjetivo() == null) {
            return new ModelAndView("redirect:/objetivo");
        }

        try {
            DatosRutina rutina = servicioRutina.getRutinaActualDelUsuario(usuario);
            modelAndView.addObject("rutina", rutina);
            modelAndView.setViewName("rutina");
        } catch (Exception e) {
            modelAndView.setViewName("redirect:/rutinas?objetivo=" + usuario.getObjetivo().toString());
            modelAndView.addObject("objetivoFormateado", usuario.getObjetivo().formatear());
        }

        return modelAndView;
    }

    @RequestMapping(path = "/asignar-rutina", method = RequestMethod.GET)
    public ModelAndView asignarRutina(@RequestParam("id") Long id, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView();

        try {
            // Obtener el usuario de la sesión
            Usuario usuario = (Usuario) session.getAttribute("usuario");

            // Obtener la rutina por su ID
            Rutina rutina = servicioRutina.getRutinaById(id);
            DatosRutina datosRutina = servicioRutina.getDatosRutinaById(id);

            // Verificar si el usuario y la rutina existen
            if (usuario != null && rutina != null) {
                // Asignar la rutina al usuario
                servicioRutina.asignarRutinaAUsuario(rutina, usuario);

                // Agregar la rutina al modelo
                modelAndView.addObject("rutina", datosRutina);
                modelAndView.setViewName("rutina");
            } else {
                // Si el usuario o la rutina no existen, redirigir a la página de rutinas
                modelAndView.setViewName("redirect:/objetivo");
            }
        } catch (Exception e) {
            // Si ocurre una excepción, redirigir a la página de rutinas
            modelAndView.setViewName("redirect:/objetivo");
        }

        return modelAndView;
    }

    @PostMapping("/liberar-rutina")
    public ModelAndView liberarRutina(@RequestParam("id") Long id, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView();

        try {
            // Obtener el usuario de la sesión
            Usuario usuario = (Usuario) session.getAttribute("usuario");

            // Obtener la rutina por su ID
            Rutina rutina = servicioRutina.getRutinaById(id);
            DatosRutina datosRutina = servicioRutina.getDatosRutinaById(id);
            modelAndView.addObject("rutina", datosRutina);

            // Verificar si el usuario y la rutina existen
            if (usuario != null && rutina != null) {
                // Libera la rutina activa del usuario
                servicioRutina.liberarRutinaActivaDelUsuario(usuario);

                // Redirigir a la sección de rutinas en base al objetivo del usuario
                modelAndView.setViewName("redirect:/rutinas?objetivo=" + usuario.getObjetivo().toString());
                modelAndView.addObject("info", "Rutina liberada.");
                modelAndView.addObject("objetivoFormateado", usuario.getObjetivo().formatear());
            } else {
                // Si el usuario o la rutina no existen, redirigir a la página de objetivos
                modelAndView.addObject("error", "Error al liberar la rutina.");
                modelAndView.setViewName("rutina");
            }
        } catch (Exception e) {
            // Si ocurre una excepción, redirigir a la página de objetivos con el mensaje de error
            modelAndView.addObject("error", "EXCEPCION: error al liberar la rutina.");
            modelAndView.setViewName("rutina");
        }

        return modelAndView;
    }


}



