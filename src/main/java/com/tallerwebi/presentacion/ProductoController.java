package com.tallerwebi.presentacion;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ProductoController {

    private List<ProductoDto> productos;

    public ProductoController() {
        this.productos = new ArrayList<ProductoDto>();
    }

    @PostMapping(path = "/carritoDeCompras")
    public ModelAndView mostrarVistaCarritoDeCompras() {
        ModelMap model = new ModelMap();

//        model.put("mensaje", "El producto fue agregado al carrito correctamente!");

        return new ModelAndView("carritoDeCompras", model);

    }
}
