package com.tallerwebi.presentacion;


import com.tallerwebi.dominio.Categorias;
import com.tallerwebi.dominio.ServicioCategorias;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
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

    @RequestMapping(path = "/categorias/{nombreDeCategoriaEnUrl}", method = RequestMethod.GET)

    //Usamos PathVariable para capturar el valor que viene en la URL
    public String verCategoria(@PathVariable("nombreDeCategoriaEnUrl") String nombreDeCategoriaEnUrl, Model model) {

        Categorias categoria = servicioCategorias.buscarCategoriaPorNombre(nombreDeCategoriaEnUrl);

        model.addAttribute("categoria", categoria);
        return "pagina-categoria-seleccionada";
    }



}
