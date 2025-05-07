package com.tallerwebi.presentacion;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CarritoController {

    private List<ProductoDto> productos;
    private Double valorTotal = 0.0;

    public CarritoController() {
        this.productos = new ArrayList<ProductoDto>();
        this.productos.add(new ProductoDto("Mouse inalámbrico", 29.99));
        this.productos.add(new ProductoDto("Teclado mecánico", 79.99));
    }

    public List<ProductoDto> getProductos() {
        return productos;
    }

    public ProductoDto buscarPorId(Long id){
        for(ProductoDto productoDto : this.productos){
            if(productoDto.getId().equals(id)){
                return productoDto;
            }
        }
        return null;
    }

    @GetMapping(path = "/carritoDeCompras")
    public ModelAndView mostrarVistaCarritoDeCompras() {
        ModelMap model = new ModelMap();
        model.put("productos", this.productos);
        Double total = calcularValorTotalDeLosProductos();
        model.put("valorTotal", total);
        return new ModelAndView("carritoDeCompras", model);

    }

    @PostMapping(path = "/home")
    public ModelAndView agregarProductoAlCarrito(@ModelAttribute("productoDto") ProductoDto producto) {
        ModelMap model = new ModelMap();
        model.put("mensaje", "El producto fue agregado al carrito correctamente!");
        model.put("productoDto", producto);
        model.put("productos", this.productos);

        Double total = calcularValorTotalDeLosProductos();
        model.put("valorTotal", total);

        return new ModelAndView("carritoDeCompras", model);
    }


    @PostMapping(path = "/carritoDeCompras")
    public ModelAndView eliminarProductoDelCarrito(Long id) {
        ModelMap model = new ModelMap();
        ProductoDto productoBuscado = buscarPorId(id);
        if(productoBuscado != null){
            this.productos.remove(productoBuscado);
            model.put("mensaje", "El producto fue eliminado!");
        } else {
            model.put("mensaje", "El producto no pudo ser eliminado!");
        }
        model.put("productos", this.productos);

        Double total = calcularValorTotalDeLosProductos();
        model.put("valorTotal", total);

        return new ModelAndView("carritoDeCompras", model);
    }

    public Double calcularValorTotalDeLosProductos() {
        Double total = 0.0;
        for(ProductoDto productoDto : this.productos){
            total += productoDto.getPrecio();
        }
        BigDecimal valorTotalConDosDecimales = new BigDecimal(total);

        valorTotalConDosDecimales = valorTotalConDosDecimales.setScale(2, RoundingMode.UP);
        this.valorTotal = valorTotalConDosDecimales.doubleValue();
        return this.valorTotal;
    }
}
