package com.tallerwebi.presentacion;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ProductoController {

    private List<ProductoDto> productos;

    public ProductoController() {
        this.productos = new ArrayList<ProductoDto>();
        productos.add(new ProductoDto("Coca-Cola", 150.0));

    }

    @GetMapping(path = "/carrito")
    public ModelAndView mostrarVistaCarritoDeCompras() {
        ModelMap model = new ModelMap();

//        model.put("mensaje", "El producto fue agregado al carrito correctamente!");
        model.addAttribute("productos", productos);
        return new ModelAndView("carrito", model);

    }
}
