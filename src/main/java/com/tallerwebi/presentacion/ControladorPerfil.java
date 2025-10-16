package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioPerfil;
import com.tallerwebi.dominio.ServicioSubasta;
import com.tallerwebi.dominio.Subasta;
import com.tallerwebi.dominio.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class ControladorPerfil {

    private final ServicioPerfil servicioPerfil;
    private final ServicioSubasta servicioSubasta;

    @Autowired
    public ControladorPerfil(ServicioPerfil servicioPerfil, ServicioSubasta servicioSubasta) {
        this.servicioPerfil = servicioPerfil;
        this.servicioSubasta = servicioSubasta;
    }

    @GetMapping("/perfil")
    public String verPerfil(HttpServletRequest request, ModelMap model) {
        String email = (String) request.getSession().getAttribute("email");
        if (email == null) {
            return "redirect:/login";
        }

        Usuario usuario = servicioPerfil.obtenerPerfil(email);
        model.put("usuario", usuario);

        List<Subasta> subastasUsuario = servicioSubasta.listarSubastasDelUsuario(email);
        model.put("subastas", subastasUsuario);

        return "perfil";
    }

    @PostMapping("/perfil")
    public String actualizarPerfil(@ModelAttribute("usuario") Usuario usuario, HttpServletRequest request) {
        String email = (String) request.getSession().getAttribute("email");
        if (email == null) {
            return "redirect:/login";
        }

        Usuario usuarioActual = servicioPerfil.obtenerPerfil(email);
        usuarioActual.setNombre(usuario.getNombre());
        servicioPerfil.actualizarPerfil(usuarioActual);

        return "redirect:/perfil";
    }
}