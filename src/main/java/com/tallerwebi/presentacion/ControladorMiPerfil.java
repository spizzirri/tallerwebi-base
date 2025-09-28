package com.tallerwebi.presentacion;


import com.tallerwebi.dominio.Usuario;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
@Controller
public class ControladorMiPerfil {

    @GetMapping("/miPerfil")
    public ModelAndView miPerfil(HttpServletRequest request) {
        Usuario usuario = (Usuario) request.getSession().getAttribute("usuarioLogueado");
        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }

        DatosUsuario datosUsuario = new DatosUsuario();
        datosUsuario.setNombre(usuario.getNombre());
        datosUsuario.setApellido(usuario.getApellido());
        datosUsuario.setCarreras(usuario.getCarreras());

        ModelMap model = new ModelMap();
        model.addAttribute("usuario", datosUsuario);

        return new ModelAndView("miPerfil", model);
    }
}