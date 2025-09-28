package com.tallerwebi.presentacion;


import com.tallerwebi.dominio.Publicacion;
import com.tallerwebi.dominio.ServicioPublicado;
import com.tallerwebi.dominio.Usuario;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ControladorHome {

    private final ServicioPublicado servicioPublicado;

    public ControladorHome(ServicioPublicado servicioPublicado) {
        this.servicioPublicado = servicioPublicado;
    }

    @GetMapping("/home")
    public ModelAndView home(HttpServletRequest request) {
        ModelMap model = new ModelMap();

        Usuario usuario = (Usuario) request.getSession().getAttribute("usuarioLogueado");

        if (usuario != null) {
            // Crear DTO
            DatosUsuario datosUsuario = new DatosUsuario();
            datosUsuario.setNombre(usuario.getNombre());
            datosUsuario.setApellido(usuario.getApellido());
            datosUsuario.setCarreras(usuario.getCarreras());

            model.addAttribute("usuario", datosUsuario);

            // Opcional: formulario vacío para publicar
            model.addAttribute("publicacion", new Publicacion());

            // Lista de publicaciones
            model.addAttribute("publicaciones", servicioPublicado.findAll());

            // Retornar la vista home con el model
            return new ModelAndView("home", model);

        } else {
            // Usuario no logueado → redirect al login
            return new ModelAndView("redirect:/login");
        }
    }
}
