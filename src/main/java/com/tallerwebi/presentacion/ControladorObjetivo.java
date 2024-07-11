package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.usuario.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Controller
public class ControladorObjetivo {

    private final DataSource dataSource;

    @Autowired
    public ControladorObjetivo(DataSource dataSource) {
        this.dataSource = dataSource;
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

}
