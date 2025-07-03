package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Usuario;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpSession;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("datosLogin")
    public DatosLoginDto datosLoginDto() {
        return new DatosLoginDto();
    }

    @ModelAttribute("usuarioLogueado")
    public UsuarioDto usuarioLogueado(HttpSession session) {
        return (UsuarioDto) session.getAttribute("usuario"); // O el atributo real que usás
    }

//    @ModelAttribute("estaLogueado")
//    public boolean estaLogueado(HttpSession session) {
//        return session.getAttribute("usuario") != null;
//    }
}