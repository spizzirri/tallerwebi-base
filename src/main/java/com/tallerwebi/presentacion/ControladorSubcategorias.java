package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.RepositorioSubcategorias;
import com.tallerwebi.dominio.ServicioSubcategorias;
import com.tallerwebi.dominio.Subcategoria;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ControladorSubcategorias {

    private final ServicioSubcategorias servicioSubcategorias;

    public ControladorSubcategorias(RepositorioSubcategorias repositorioSubcategorias, ServicioSubcategorias servicioSubcategorias) {
        this.servicioSubcategorias = servicioSubcategorias;
    }

    @RequestMapping(path = "/categorias/{nombreDeCategoriaEnUrl}/{nombreDeSubcategoriaEnUrl}", method = RequestMethod.GET)

    public String verSubcategorias(@PathVariable("nombreDeCategoriaEnUrl") String nombreDeCategoriaEnUrl,
                                   @PathVariable("nombreDeSubcategoriaEnUrl") String nombreDeSubcategoriaEnUrl,
                                   Model model) {

        Subcategoria subcategoria = servicioSubcategorias.buscarSubcategoriaPorNombreDeRuta(nombreDeCategoriaEnUrl,nombreDeSubcategoriaEnUrl);
        model.addAttribute("subcategoria", subcategoria);

        return "pagina-subcategoria-seleccionada";
    }
}
