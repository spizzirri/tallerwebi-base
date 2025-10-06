package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.RepositorioSubcategorias;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ControladorSubcategorias {

    private final RepositorioSubcategorias repositorioSubcategorias;

    public ControladorSubcategorias(RepositorioSubcategorias repositorioSubcategorias) {
        this.repositorioSubcategorias = repositorioSubcategorias;
    }

    @RequestMapping(path = "/categorias/{nombreDeCategoriaEnUrl}/{nombreDeSubcategoriaEnUrl}", method = RequestMethod.GET)

    public String verSubcategorias(@PathVariable("nombreDeCategoriaEnUrl") String nombreDeCategoriaEnUrl,
                                   @PathVariable("nombreDeSubcategoriaEnUrl") String nombreDeSubcategoriaEnUrl,
                                   Model model) {
        return "pagina-categoria-seleccionada";
    }


}
