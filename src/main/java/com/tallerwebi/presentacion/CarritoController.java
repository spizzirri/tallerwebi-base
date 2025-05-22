package com.tallerwebi.presentacion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CarritoController {
    private static final Logger logger = LoggerFactory.getLogger(CarritoController.class);

    private List<ProductoCarritoDto> productos;
    private Double valorTotal = 0.0;
    private Double valorTotalConDescuento = 0.0;

    public CarritoController() {
        this.productos = new ArrayList<ProductoCarritoDto>();
        this.productos.add(new ProductoCarritoDto("Mouse inalámbrico", 29.99));
        this.productos.add(new ProductoCarritoDto("Teclado mecánico", 79.99));
    }

    public List<ProductoCarritoDto> getProductos() {
        return productos;
    }

    public ProductoCarritoDto buscarPorId(Long id){
        for(ProductoCarritoDto productoDto : this.productos){
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
    public ModelAndView agregarProductoAlCarrito(@ModelAttribute("productoDto") ProductoCarritoDto producto) {
        ModelMap model = new ModelMap();
        model.put("mensaje", "El producto fue agregado al carrito correctamente!");
        model.put("productoDto", producto);
        model.put("productos", this.productos);

        Double total = calcularValorTotalDeLosProductos();
        model.put("valorTotal", total);

        return new ModelAndView("carritoDeCompras", model);
    }


    @PostMapping(path = "/carritoDeCompras/eliminarProducto/{id}")
    @ResponseBody
    public Map<String, Object> eliminarProductoDelCarrito(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        ProductoCarritoDto productoBuscado = buscarPorId(id);

        if(productoBuscado != null){
            this.productos.remove(productoBuscado);
            response.put("eliminado", true);
        } else {
            response.put("eliminado", false);
        }
        response.put("productos", this.productos);

        Double total = calcularValorTotalDeLosProductos();
        response.put("valorTotal", total);

        return response;
    }

    public Double calcularValorTotalDeLosProductos() {
        Double total = 0.0;
        for(ProductoCarritoDto productoDto : this.productos){
            total += productoDto.getPrecio() * productoDto.getCantidad();
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

    @PostMapping(path = "/carritoDeCompras/aplicarDescuento")
    @ResponseBody //este metodo no retorna nada, solo se usa para enviar un mensaje de respuesta al cliente cuando se aplica un descuento
    public Map<String, String> calcularValorTotalDeLosProductosConDescuento(@RequestBody Map<String, String> codigoDescuentoMap) {
        String codigoDescuento = codigoDescuentoMap.get("codigoInput");

        Double total = calcularValorTotalDeLosProductos();
        this.valorTotalConDescuento = total;
        Integer codigoDescuentoExtraido = extraerPorcentajeDesdeCodigoDeDescuento(codigoDescuento);

        Map<String, String> response = new HashMap<>();

        if(codigoDescuentoExtraido == null || !(codigoDescuentoExtraido == 5 || codigoDescuentoExtraido == 10 || codigoDescuentoExtraido == 15)){
            response.put("mensajeDescuento", "Codigo de descuento invalido!"); // muestro un mensaje cuando el codigo no tenrmina en numero o es distinto de los validos para aplicar el descuento
            return response;
        }

        switch (codigoDescuentoExtraido) {
            case 5:
                this.valorTotalConDescuento = total - (total * 0.05);
                break;
            case 10:
                this.valorTotalConDescuento = total - (total * 0.10);
                break;
            case 15:
                this.valorTotalConDescuento = total - (total * 0.15);
                break;
        }

        BigDecimal valorTotalConDosDecimales = new BigDecimal(this.valorTotalConDescuento);
        valorTotalConDosDecimales = valorTotalConDosDecimales.setScale(2, RoundingMode.UP);
        this.valorTotalConDescuento = valorTotalConDosDecimales.doubleValue();
        response.put("mensaje", "Valor con descuento: " + this.valorTotalConDescuento);
        return response;
    }


    @PostMapping(path = "/carritoDeCompras/formularioDePago")
    @ResponseBody
    public Map<String, Object> procesarCompra(@RequestParam(value = "metodoPago") String metodoDePago) {
        Map<String, Object> response = new HashMap<>();
        logger.info("Procesando compra con método de pago: {}", metodoDePago);

        if(metodoDePago == null || metodoDePago.isEmpty()){
            response.put("success", false);
            response.put("error", "Debes seleccionar un metodo de pago");
            response.put("mostrarModal", false);
            logger.warn("Error en procesamiento de compra: método de pago no seleccionado");
        } else {
            response.put("success", true);
            response.put("mostrarModal", true);
            response.put("metodoPago", metodoDePago);
            logger.info("Método de pago seleccionado correctamente: {}", metodoDePago);
        }
        return response;
    }


    @PostMapping(path = "/carritoDeCompras/agregarMasCantidadDeUnProducto/{id}")
    @ResponseBody
    public Map<String, Object> agregarMasCantidadDeUnProducto(@PathVariable Long id) {
        ProductoCarritoDto productoBuscado = buscarPorId(id);

        if (productoBuscado != null) {
            productoBuscado.setCantidad(productoBuscado.getCantidad() + 1);
            calcularValorTotalDeLosProductos();
        }

        Double valorTotalDelProductoBuscado = productoBuscado.getCantidad() * productoBuscado.getPrecio();

        Map<String, Object> response = new HashMap<>();
        assert productoBuscado != null;
        response.put("cantidad", productoBuscado.getCantidad());
        response.put("precioTotalDelProducto", valorTotalDelProductoBuscado);
        response.put("valorTotal", this.valorTotal);
        return response;

    }

    @PostMapping("/carritoDeCompras/restarCantidadDeUnProducto/{id}")
    @ResponseBody
    public Map<String, Object> restarCantidadDeUnProducto(@PathVariable Long id) {
        ProductoCarritoDto productoBuscado = buscarPorId(id);
        Map<String, Object> response = new HashMap<>();
        if(productoBuscado != null && productoBuscado.getCantidad() > 1){
            productoBuscado.setCantidad(productoBuscado.getCantidad() - 1);
            calcularValorTotalDeLosProductos();
            Double valorTotalDelProductoBuscado = productoBuscado.getCantidad() * productoBuscado.getPrecio();
            response.put("cantidad", productoBuscado.getCantidad());
            response.put("precioTotalDelProducto", valorTotalDelProductoBuscado);
            response.put("valorTotal", this.valorTotal);
            return response;
        } else {
            this.productos.remove(productoBuscado);
            response.put("eliminado", true);
        }
        assert productoBuscado != null;
        return response;
    }
}
