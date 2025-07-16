package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioCategorias;
import com.tallerwebi.dominio.ServicioBuscarProducto;
import com.tallerwebi.dominio.ServicioPrecios;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Controller
public class ControladorMostrarProductos {

    private ServicioCategorias categoriasService;
    private ServicioBuscarProducto productoService;
    private ServicioPrecios servicioPrecios;


//    #1 Equipos y Notebook
//    #2 Monitores
//    #3 Televisores
//    #4 Placas de Video
//    #5 Sillas Gamers
//    #6 Perificos
//    #7 Mothers
//    #8 Procesadores

    @Autowired
    public ControladorMostrarProductos(ServicioBuscarProducto productoService, ServicioCategorias categoriasService, ServicioPrecios servicioPrecios) {
        this.productoService = productoService;
        this.categoriasService = categoriasService;
        this.servicioPrecios = servicioPrecios;
    }


    @GetMapping("/productos")
    public ModelAndView showProductos() {
        ModelMap model = new ModelMap();
        List<ProductoDto> productos = productoService.getProductosEnStock();
        Collections.shuffle(productos);
        List<ProductoDto> productosLimitados = productos.stream().limit(24).collect(Collectors.toList());
        List<CategoriaDto> categorias = categoriasService.getCategorias();
        model.put("categorias", categorias);

        for (ProductoDto producto : productos) {
            String totalFormateado = this.servicioPrecios.conversionDolarAPeso(producto.getPrecio());
            producto.setPrecioFormateado(totalFormateado);
        }

        model.addAttribute("productos", productosLimitados);

        return new ModelAndView("productos", model);
    }

    @GetMapping("/productos/search")
    public ModelAndView buscarProductos(@RequestParam(value = "q", required = false) String query, @RequestParam(value = "cat", required = false) String categoria) throws ClassNotFoundException {
        ModelMap model = new ModelMap();
        List<CategoriaDto> categorias = categoriasService.getCategorias();
        List<ProductoDto> productos;
        if ((query != null && !query.isBlank()) && (categoria != null && !categoria.isBlank())) {
            productos = productoService.getProductosPorTipoYPorQuery(categoria, query);
        } else if (query != null && !query.isBlank()) {
            productos = productoService.getProductosPorQuery(query);
        } else if (categoria != null && !categoria.isBlank()) {
            productos = productoService.getProductosPorCategoria(categoria);
        } else {
            productos = productoService.getProductosEnStock(); // opcional
            model.addAttribute("mensaje", "Mostrando todos los productos.");
        }

        for (ProductoDto producto : productos) {
            String totalFormateado = this.servicioPrecios.conversionDolarAPeso(producto.getPrecio());
            producto.setPrecioFormateado(totalFormateado);
        }

        model.put("productosBuscados", productos);
        model.put("categorias", categorias);

        if ((query != null || categoria != null) && productos.isEmpty()) {
            model.addAttribute("productosBuscados", false);
            model.addAttribute("mensaje", "No se encontraron productos para esa búsqueda. Mostrando todos los productos.");
            List<ProductoDto> productos2 = productoService.getProductosEnStock();
            model.put("productos", productos2);
        }

        return new ModelAndView("productos", model);
    }


}

