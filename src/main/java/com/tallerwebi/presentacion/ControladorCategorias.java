package com.tallerwebi.presentacion;


import com.tallerwebi.dominio.ServicioCategorias;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ControladorCategorias {

    private final ServicioCategorias servicioCategorias;

    public ControladorCategorias(ServicioCategorias servicioCategorias) {
        this.servicioCategorias = servicioCategorias;
    }

    @RequestMapping(path = "/categorias", method = RequestMethod.GET)
    public String mostrarCategoriasExistentes(Model model) {
        model.addAttribute("categorias", servicioCategorias.listarCategorias());
        return "categorias";
    }

}
