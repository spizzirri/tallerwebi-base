package com.tallerwebi.presentacion;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ProductoController {

    private List<ProductoDto> productos;

    public ProductoController() {
        this.productos = new ArrayList<ProductoDto>();
        this.productos.add(new ProductoDto("Mouse inalámbrico", 29.99));
        this.productos.add(new ProductoDto("Teclado mecánico", 79.99));

    }

    public List<ProductoDto> getProductos() {
        return productos;
    }

    @GetMapping(path = "/carritoDeCompras")
    public ModelAndView mostrarVistaCarritoDeCompras() {
        ModelMap model = new ModelMap();
        model.put("productos", productos);
        return new ModelAndView("carritoDeCompras", model);

    }

    @PostMapping(path = "/carritoDeCompras")
    public ModelAndView agregarProductoAlCarrito(@ModelAttribute("productoDto") ProductoDto producto) {
        ModelMap model = new ModelMap();
        model.put("mensaje", "El producto fue agregado al carrito correctamente!");
        model.put("productoDto", producto);
        model.put("productos", productos);

        return new ModelAndView("carritoDeCompras", model);
    }
}
