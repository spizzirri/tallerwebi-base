package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.usuario.ServicioLogin;
import com.tallerwebi.dominio.usuario.Usuario;
import com.tallerwebi.dominio.calendario.ServicioCalendario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.reto.Reto;
import com.tallerwebi.dominio.reto.ServicioReto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class ControladorLogin {

    private final ServicioLogin servicioLogin;
    private ServicioCalendario servicioCalendario;
    private ServicioReto servicioReto;

    @Autowired
    public ControladorLogin(ServicioLogin servicioLogin){
        this.servicioLogin = servicioLogin;
    }

    @RequestMapping("/login")
    public ModelAndView irALogin() {

        ModelMap modelo = new ModelMap();
        modelo.put("datosLogin", new DatosLogin());
        return new ModelAndView("login", modelo);
    }

    @RequestMapping(path = "/validar-login", method = RequestMethod.POST)
    public ModelAndView validarLogin(@ModelAttribute("datosLogin") DatosLogin datosLogin, HttpServletRequest request) {
        ModelMap model = new ModelMap();
        Usuario usuarioBuscado = servicioLogin.consultarUsuario(datosLogin.getEmail(), datosLogin.getPassword());
        if (usuarioBuscado != null) {
            request.getSession().setAttribute("usuario", usuarioBuscado);
            request.getSession().setAttribute("ROL", usuarioBuscado.getRol());
            return new ModelAndView("redirect:/home");
        } else {
            model.put("error", "Usuario o clave incorrecta");
        }
        return new ModelAndView("login", model);
    }


    @RequestMapping(path = "/registrarme", method = RequestMethod.POST)
    public ModelAndView registrarme(@ModelAttribute("usuario") Usuario usuario) {
        ModelMap model = new ModelMap();
        try{
            servicioLogin.registrar(usuario);
        } catch (UsuarioExistente e){
            model.put("error", "El usuario ya existe");
            return new ModelAndView("nuevo-usuario", model);
        } catch (Exception e){
            model.put("error", "al registrar el nuevo usuario");
            return new ModelAndView("nuevo-usuario", model);
        }
        return new ModelAndView("redirect:/login");
    }

    @RequestMapping(path = "/nuevo-usuario", method = RequestMethod.GET)
    public ModelAndView nuevoUsuario() {
        ModelMap model = new ModelMap();
        model.put("usuario", new Usuario());
        return new ModelAndView("nuevo-usuario", model);
    }

    @RequestMapping(path = "/home", method = RequestMethod.GET)
    public ModelAndView irAHome(HttpSession session){
        ModelAndView modelAndView = new ModelAndView("home");
        // Obtener el usuario de la sesión
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            modelAndView.setViewName("redirect:/login");
        }
        modelAndView.addObject("usuario", usuario);
        // Obtener el ItemRendimiento más seleccionado
        DatosItemRendimiento itemMasSeleccionado = servicioLogin.obtenerItemMasSeleccionado();

        // Añadir el ItemRendimiento más seleccionado al modelo
        modelAndView.addObject("itemMasSeleccionado", itemMasSeleccionado);

        // Verificar si hay un reto en proceso desde el servicio
        Reto retoEnProceso = servicioLogin.obtenerRetoEnProceso();
        if (retoEnProceso != null) {
            modelAndView.addObject("retoDisponible", retoEnProceso);
            long minutosRestantes = servicioLogin.calcularTiempoRestante(retoEnProceso.getId());
            modelAndView.addObject("minutosRestantes", minutosRestantes);
        } else {
            // Verificar si hay un reto en la sesión
            Reto retoDisponible = (Reto) session.getAttribute("retoDisponible");
            if (retoDisponible == null) {
                // Obtener un nuevo reto disponible si no hay uno en la sesión
                retoDisponible = servicioLogin.obtenerRetoDisponible();
                // Guardar el nuevo reto disponible en la sesión
                session.setAttribute("retoDisponible", retoDisponible);
            }
            modelAndView.addObject("retoDisponible", retoDisponible);
            session.setAttribute("retoDisponible", retoDisponible);
        }
        return modelAndView;
    }


    @RequestMapping(path = "/", method = RequestMethod.GET)
    public ModelAndView inicio() {
        return new ModelAndView("redirect:/login");
    }

};

