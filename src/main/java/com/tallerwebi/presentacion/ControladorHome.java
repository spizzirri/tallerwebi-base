package com.tallerwebi.presentacion;


import com.tallerwebi.dominio.Publicacion;
import com.tallerwebi.dominio.ServicioLike;
import com.tallerwebi.dominio.ServicioPublicado;
import com.tallerwebi.dominio.Usuario;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ControladorHome {

    private final ServicioPublicado servicioPublicado;
    private final ServicioLike servicioLike;

    public ControladorHome(ServicioPublicado servicioPublicado, ServicioLike servicioLike) {
        this.servicioPublicado = servicioPublicado;
        this.servicioLike = servicioLike;
    }

    @GetMapping("/home")
    public ModelAndView home(HttpServletRequest request) {
        ModelMap model = new ModelMap();
        HttpSession session = request.getSession();

        // Obtener usuario logueado de la sesión
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        System.out.println("ID de sesión: " + session.getId());
        System.out.println("Usuario en sesión en /home: " + (usuario != null ? usuario.getEmail() : "NULL"));

        if (usuario != null) {
            // Crear DTO
            DatosUsuario datosUsuario = new DatosUsuario();
            datosUsuario.setNombre(usuario.getNombre());
            datosUsuario.setApellido(usuario.getApellido());
            datosUsuario.setCarreras(usuario.getCarreras());

            model.addAttribute("usuario", datosUsuario);

            //  formulario vacío para publicar
            model.addAttribute("publicacion", new Publicacion());


            List<Publicacion> publicaciones = servicioPublicado.findAll();
            Map<Long, Integer> likesPorPublicacion = new HashMap<>();

            for (Publicacion p : publicaciones) {
                likesPorPublicacion.put(p.getId(),servicioLike.contarLikes(p));
            }

            model.addAttribute("publicaciones", publicaciones);
            model.addAttribute("likesPorPublicacion", likesPorPublicacion);

            return new ModelAndView("home", model);
        } else {
            return new ModelAndView("redirect:/login");
        }
    }
}
