package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.excepcion.ListaDeRutinasVaciaException;
import com.tallerwebi.dominio.rutina.EstadoEjercicio;
import com.tallerwebi.dominio.usuario.ServicioLogin;
import com.tallerwebi.dominio.usuario.Usuario;
import com.tallerwebi.dominio.objetivo.Objetivo;
import com.tallerwebi.dominio.rutina.Rutina;
import com.tallerwebi.dominio.rutina.ServicioRutina;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
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
    public ModelAndView verRutinasQueLeInteresanAlUsuario( HttpSession session) throws ListaDeRutinasVaciaException {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }

        String objetivo = usuario.getObjetivo().toString();


        if (objetivo == null || objetivo.isEmpty()) {
            ModelAndView modelAndView = new ModelAndView("objetivo");
            modelAndView.addObject("error", "Objetivo no válido");
            modelAndView.setViewName("objetivo");
            return modelAndView;
        }

        ModelAndView modelAndView = new ModelAndView("rutinas");
        modelAndView.addObject("usuario", usuario);

        Objetivo objetivoEnum;
        try {
            objetivoEnum = Objetivo.valueOf(objetivo);
        } catch (IllegalArgumentException e) {
            modelAndView.setViewName("objetivo");
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
        modelAndView.addObject("usuario", usuario);

        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }

        if (usuario.getObjetivo() == null) {
            return new ModelAndView("redirect:/objetivo");
        }

        try {
            DatosRutina rutina = servicioRutina.getRutinaActualDelUsuario(usuario);
            List<EstadoEjercicio> estadosEjercicios = servicioRutina.getEstadosEjercicios(usuario, rutina);
            modelAndView.addObject("rutina", rutina);
            modelAndView.addObject("estadosEjercicios", estadosEjercicios);
            modelAndView.setViewName("rutina");
        } catch (Exception e) {
            modelAndView.setViewName("redirect:/rutinas");
        }

        return modelAndView;
    }

    @RequestMapping(path = "/asignar-rutina", method = RequestMethod.GET)
    public ModelAndView asignarRutina(@RequestParam("id") Long id, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView();

        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            modelAndView.addObject("usuario", usuario);
            Rutina rutina = servicioRutina.getRutinaById(id);
            DatosRutina datosRutina = servicioRutina.getDatosRutinaById(id);

            if (usuario != null && rutina != null) {
                servicioRutina.asignarRutinaAUsuario(rutina, usuario);

                modelAndView.addObject("rutina", datosRutina);
                modelAndView.setViewName("redirect:/mi-rutina");
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
    public ModelAndView liberarRutina(HttpSession session) {
        ModelAndView modelAndView = new ModelAndView();

        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            DatosRutina rutinaActualDelUsuario = servicioRutina.getRutinaActualDelUsuario(usuario);
            modelAndView.addObject("usuario", usuario);
            modelAndView.addObject("rutina", rutinaActualDelUsuario);

            if (usuario != null && rutinaActualDelUsuario != null) {
                servicioRutina.liberarRutinaActivaDelUsuario(usuario);

                modelAndView.setViewName("redirect:/rutinas");
                modelAndView.addObject("info", "Rutina liberada.");
            } else {
                modelAndView.addObject("error", "Error al liberar la rutina.");
                modelAndView.setViewName("rutina");
            }
        } catch (Exception e) {
            modelAndView.addObject("error", "EXCEPCION: error al liberar la rutina.");
            modelAndView.setViewName("rutina");
        }

        return modelAndView;
    }

    @PostMapping("/actualizar-estado-ejercicio")
    public String actualizarEstadoEjercicio(@RequestParam("idEjercicio") Long idEjercicio, @RequestParam("estado") EstadoEjercicio.Estado estado, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        servicioRutina.actualizarEstadoEjercicio(usuario, idEjercicio, estado);
        return "redirect:/mi-rutina";
    }

    @GetMapping("/objetivo")
    public ModelAndView mostrarVistaObjetivos(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }
        ModelAndView modelAndView = new ModelAndView("objetivo");
        modelAndView.addObject("usuario", usuario);

        return modelAndView;
    }

    @RequestMapping(path = "/guardar-objetivo", method = RequestMethod.GET)
    public ModelAndView guardarObjetivo(@RequestParam("objetivo") String objetivo, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }

        ModelAndView modelAndView = new ModelAndView("objetivo");

        try {
            Objetivo objetivoEnum = Objetivo.valueOf(objetivo);
            servicioRutina.guardarObjetivoEnUsuario(objetivoEnum,usuario);

        } catch (Exception e) {
            modelAndView.setViewName("objetivo");
            modelAndView.addObject("Excepcion:", e.getMessage());
            return modelAndView;
        }

        return new ModelAndView("redirect:/rutinas");
    }


}



