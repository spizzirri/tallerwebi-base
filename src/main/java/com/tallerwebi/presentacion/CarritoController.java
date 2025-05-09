package com.tallerwebi.presentacion;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CarritoController {

    private List<ProductoDto> productos;
    private Double valorTotal = 0.0;
    private Double valorTotalConDescuento = 0.0;

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
//        model.put("mensajeDescuento", "Descuento aplicado correctamente: $" + total ); //mensaje de prueba para el codigo de descuento
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
        valorTotalConDosDecimales = valorTotalConDosDecimales.setScale(2, RoundingMode.UP); //convierto el numero para que tenga dos decimales y redondee para arriba
        this.valorTotal = valorTotalConDosDecimales.doubleValue();

        return this.valorTotal;
    }

    private Integer extraerPorcentajeDesdeCodigoDeDescuento(String codigoDescuento){
        if(codigoDescuento == null || codigoDescuento.isEmpty()){
            return null;
        }

        String numeroExtraido = codigoDescuento.replaceAll("^.*?(\\d+)$", "$1"); //reemplaza el texto que coincide con un patrón. En este caso se queda con los numeros que hay despues de un string

        try {
          return Integer.parseInt(numeroExtraido); //convierte el numero extraido a entero
        } catch (NumberFormatException e) {
            return null; //si el final del string no es un numero lanza una exepcion
        }
    }

    @GetMapping(path = "/carritoDeCompras/descuento")
    public ModelAndView calcularValorTotalDeLosProductosConDescuento(String codigoDescuento) {
        ModelMap model = new ModelMap();

        Double total = calcularValorTotalDeLosProductos();
        Integer codigoDescuentoExtraido = extraerPorcentajeDesdeCodigoDeDescuento(codigoDescuento);

        if(codigoDescuentoExtraido == null || !(codigoDescuentoExtraido == 5 || codigoDescuentoExtraido == 10 || codigoDescuentoExtraido == 15)){
            model.put("mensajeDescuento", "Codigo de descuento invalido!"); // muestro un mensaje cuando el codigo no tenrmina en numero o es distinto de los validos para aplicar el descuento
            return new ModelAndView("carritoDeCompras", model);
        }

        switch (codigoDescuentoExtraido) {
            case 5:
                total = total - (total * 0.05);
                break;
            case 10:
                 total = total - (total * 0.10);
                break;
            case 15:
                total = total - (total * 0.15);
                break;
        }

        BigDecimal valorTotalConDosDecimales = new BigDecimal(total);
        valorTotalConDosDecimales = valorTotalConDosDecimales.setScale(2, RoundingMode.UP);
        this.valorTotalConDescuento = valorTotalConDosDecimales.doubleValue();

        model.put("valorTotalConDescuento", this.valorTotalConDescuento);
        model.put("mensajeDescuento", "Descuento aplicado correctamente: $" + total );

        return new ModelAndView("carritoDeCompras", model);
    }

    @PostMapping(path = "/formularioPagoModal")
    public ModelAndView procesarCompra(@RequestParam(value = "metodoPago", required = false) String metodoDePago) {
        ModelMap model = new ModelMap();
        if(metodoDePago == null || metodoDePago.isEmpty()){
            model.put("error", "Debes seleccionar un metodo de pago");
            return new ModelAndView("carritoDeCompras", model);
        }
        return new ModelAndView("formularioPagoModal");
    }


    @PostMapping(path = "/carritoDeCompras/agregarMasCantidadDeUnProducto/{id}")
    public String agregarMasCantidadDeUnProducto(@PathVariable Long id) {
        ProductoDto productoBuscado = buscarPorId(id);
        if(productoBuscado != null){
            productoBuscado.setCantidad(productoBuscado.getCantidad() + 1);
        }

        assert productoBuscado != null;
        return "redirect:/carritoDeCompras";

    }

    @PostMapping("/carritoDeCompras/restarCantidadDeUnProducto/{id}")
    public String restarCantidadDeUnProducto(@PathVariable Long id) {
        ProductoDto productoBuscado = buscarPorId(id);
        if(productoBuscado != null && productoBuscado.getCantidad() > 1){
            productoBuscado.setCantidad(productoBuscado.getCantidad() - 1);
        } else {
            this.productos.remove(productoBuscado);
        }

        assert productoBuscado != null;
        return "redirect:/carritoDeCompras";
    }
}
