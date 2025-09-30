package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class ControladorPublicacion {

    private ServicioPublicado servicioPublicado;
    private ServicioLike servicioLike;

    @Autowired
    public ControladorPublicacion(ServicioPublicado servicioPublicado, ServicioLike servicioLike) {
        this.servicioPublicado = servicioPublicado;
        this.servicioLike = servicioLike;
    }

    @RequestMapping(path = "/publicaciones", method = RequestMethod.POST)
    public ModelAndView agregarPublicacion(@ModelAttribute("publicacion") Publicacion publicacion,
                                           HttpServletRequest request) throws PublicacionFallida {
        Usuario usuario = (Usuario) request.getSession().getAttribute("usuarioLogueado");

        if (usuario != null) {
            publicacion.setUsuario(usuario); // asociar el usuario
        }

        servicioPublicado.realizar(publicacion);

        return new ModelAndView("redirect:/home"); // vuelve al home
    }
//
//    @RequestMapping(path = "/nuevo-publicacion", method = RequestMethod.GET)
//    public ModelAndView mostrarPublicacion(HttpServletRequest request) {
//        ModelMap model = new ModelMap();
//
//        Usuario usuario = (Usuario) request.getSession().getAttribute("usuarioLogueado");
//        if (usuario != null) {
//            DatosUsuario datosUsuario = new DatosUsuario();
//            datosUsuario.setNombre(usuario.getNombre());
//            datosUsuario.setApellido(usuario.getApellido());
//            datosUsuario.setCarreras(usuario.getCarreras());
//
//            model.addAttribute("usuario", datosUsuario);
//        } else {
//            return new ModelAndView("redirect:/login");
//        }
//
//        model.put("publicacion", new Publicacion());
//        model.put("publicaciones", servicioPublicado.findAll()); // opcional si querés mostrar publicaciones
//
//        return new ModelAndView("home", model);
//    }
@RequestMapping(path = "/publicacion/darLike", method = RequestMethod.POST)
public ModelAndView darLike(@RequestParam Long id, HttpServletRequest request) {
    Usuario usuario = (Usuario) request.getSession().getAttribute("usuarioLogueado");

    if (usuario == null) {
        // Redirigir al login si no hay usuario
        return new ModelAndView("redirect:/login");
    }

    Publicacion publicacion = servicioPublicado.obtenerPublicacionPorId(id);
    if (publicacion != null) {
        servicioLike.darLike(usuario, publicacion);
        System.out.println("Like de usuario " + usuario.getEmail() + " a publicación " + publicacion.getId());
    }

    // Redirect para evitar repost del form
    return new ModelAndView("redirect:/home");
}

}