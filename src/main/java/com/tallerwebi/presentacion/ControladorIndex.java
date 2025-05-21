package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioBuscarProducto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.GetMapping;


import java.util.ArrayList;
import java.util.List;


@Controller
public class ControladorIndex {
    private List<ProductoDto> productos;
    @Autowired
    private ServicioBuscarProducto productoService;

//    #1 Equipos y Notebook
//    #2 Monitores
//    #3 Televisores
//    #4 Placas de Video
//    #5 Sillas Gamers
//    #6 Perificos
//    #7 Mothers
//    #8 Procesadores

    public ControladorIndex(ServicioBuscarProducto productoService) {
        this.productoService = productoService;
    }


    @GetMapping("/index")
    public ModelAndView irAHome() {
        return new ModelAndView("index");
    }

    @GetMapping("/productos/categoria/{id}")
    public ModelAndView cargarCategorias(@PathVariable Integer id) {

        ModelMap model = new ModelMap();
        List<ProductoDto> productosDestacados = productoService.buscarPorCategoria(id);

        model.addAttribute("productosDestacados", productosDestacados);
        return new ModelAndView("cargarProductosDinamicos", model);
    }

}
