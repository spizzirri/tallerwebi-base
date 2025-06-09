package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioDeEnvios;
import com.tallerwebi.dominio.ServicioProductoCarrito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@RestController
public class CarritoController {
    private static final Logger logger = LoggerFactory.getLogger(CarritoController.class);

    private final ServicioProductoCarrito productoService;
    private final ServicioDeEnvios servicioDeEnvios;

    public String codigoPostalActual;
    public EnvioDto envioActual;

    public CarritoController(ServicioProductoCarrito servicioProductoCarrito, ServicioDeEnvios servicioDeEnvios) {
        this.productoService = servicioProductoCarrito;
        this.servicioDeEnvios = servicioDeEnvios;
        servicioProductoCarrito.init();
    }

    @GetMapping(path = "/carritoDeCompras/index")
    public ModelAndView mostrarVistaCarritoDeCompras() {
        ModelMap model = new ModelMap();
        model.put("productos", this.productoService.getProductos());
        Double total = this.productoService.calcularValorTotalDeLosProductos();
        model.put("valorTotal", total);
        return new ModelAndView("carritoDeCompras", model);
    }

    @PostMapping(path = "/carritoDeCompras/home")
    public ModelAndView agregarProductoAlCarrito(@ModelAttribute("productoDto") ProductoCarritoDto producto) {
        ModelMap model = new ModelMap();
        model.put("mensaje", "El producto fue agregado al carrito correctamente!");
        model.put("productoDto", producto);
        model.put("productos", this.productoService.getProductos());

        Double total = this.productoService.calcularValorTotalDeLosProductos();
        model.put("valorTotal", total);

        return new ModelAndView("carritoDeCompras", model);
    }


    @PostMapping(path = "/carritoDeCompras/eliminarProducto/{id}")
    @ResponseBody
    public Map<String, Object> eliminarProductoDelCarrito(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        ProductoCarritoDto productoBuscado = this.productoService.buscarPorId(id);

        if (productoBuscado != null) {
            this.productoService.getProductos().remove(productoBuscado);
            response.put("eliminado", true);
        } else {
            response.put("eliminado", false);
        }
        response.put("productos", this.productoService.getProductos());

        Double total = this.productoService.calcularValorTotalDeLosProductos();
        response.put("valorTotal", total);

        return response;
    }


    private Integer extraerPorcentajeDesdeCodigoDeDescuento(String codigoDescuento) {
        if (codigoDescuento == null || codigoDescuento.isEmpty()) {
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
    @ResponseBody
    //este metodo solo se usa para enviar un mensaje de respuesta al cliente cuando se aplica un descuento
    public Map<String, Object> calcularValorTotalDeLosProductosConDescuento(@RequestBody Map<String, String> codigoDescuentoMap) {
        String codigoDescuento = codigoDescuentoMap.get("codigoInput");

        Integer codigoDescuentoExtraido = extraerPorcentajeDesdeCodigoDeDescuento(codigoDescuento);
        Map<String, Object> response = new HashMap<>();

        if (codigoDescuentoExtraido == null || !(codigoDescuentoExtraido == 5 || codigoDescuentoExtraido == 10 || codigoDescuentoExtraido == 15)) {
            response.put("mensajeDescuento", "Codigo de descuento invalido!");
            return response;
        }
        Double valorTotalConDescuento = this.productoService.calcularDescuento(codigoDescuentoExtraido);

        response.put("mensaje", "Descuento aplicado! Nuevo total: $" + valorTotalConDescuento.toString());
        response.put("valorTotal", valorTotalConDescuento);

        return response;
    }

    @PostMapping(path = "/carritoDeCompras/agregarMasCantidadDeUnProducto/{id}")
    @ResponseBody
    public Map<String, Object> agregarMasCantidadDeUnProducto(@PathVariable Long id) {
        ProductoCarritoDto productoBuscado = this.productoService.buscarPorId(id);

        if (productoBuscado != null) {
            productoBuscado.setCantidad(productoBuscado.getCantidad() + 1);
            this.productoService.calcularValorTotalDeLosProductos();
        }

        Double valorTotalDelProductoBuscado = productoBuscado.getCantidad() * productoBuscado.getPrecio();

        Map<String, Object> response = new HashMap<>();
        assert productoBuscado != null;
        response.put("cantidad", productoBuscado.getCantidad());
        response.put("precioTotalDelProducto", valorTotalDelProductoBuscado);
        response.put("valorTotal", this.productoService.valorTotal);
        return response;
    }

    @PostMapping("/carritoDeCompras/restarCantidadDeUnProducto/{id}")
    @ResponseBody
    public Map<String, Object> restarCantidadDeUnProducto(@PathVariable Long id) {
        ProductoCarritoDto productoBuscado = this.productoService.buscarPorId(id);
        Map<String, Object> response = new HashMap<>();
        if (productoBuscado != null && productoBuscado.getCantidad() > 1) {

            productoBuscado.setCantidad(productoBuscado.getCantidad() - 1);
            this.productoService.calcularValorTotalDeLosProductos();

            Double valorTotalDelProductoBuscado = productoBuscado.getCantidad() * productoBuscado.getPrecio();

            response.put("cantidad", productoBuscado.getCantidad());
            response.put("precioTotalDelProducto", valorTotalDelProductoBuscado);
            response.put("valorTotal", this.productoService.valorTotal);

            return response;
        } else {
            this.productoService.getProductos().remove(productoBuscado);
            response.put("eliminado", true);
        }
        assert productoBuscado != null;
        return response;
    }

    @PostMapping(path = "/carritoDeCompras/formularioPago")
    @ResponseBody
    public Map<String, Object> procesarCompra(@RequestParam(value = "metodoPago") String metodoDePago) {
        Map<String, Object> response = new HashMap<>();

        if (metodoDePago == null || metodoDePago.isEmpty()) {
            response.put("success", false);
            response.put("error", "Debes seleccionar un metodo de pago");
            return response;
        }

        if ("mercadoPago".equalsIgnoreCase(metodoDePago)) {
            response.put("success", true);
            response.put("metodoPago", "mercadoPago");

            if (envioActual != null && codigoPostalActual != null) {
                response.put("costoEnvio", envioActual.getCosto());
            }
        } else {
            response.put("success", true);
            response.put("metodoPago", metodoDePago);
        }

        return response;
    }

    @PostMapping(path = "/carritoDeCompras/calcularEnvio")
    public ModelAndView calcularEnvio(@RequestParam(value = "codigoPostal", required = false) String codigoPostal) {

        ModelMap model = new ModelMap();

        this.codigoPostalActual = codigoPostal;

        model.put("productos", this.productoService.getProductos());

        Double total = this.productoService.calcularValorTotalDeLosProductos();

        model.put("valorTotal", total);
        model.put("codigoPostal", codigoPostal);

        try {
            if (codigoPostal != null && !codigoPostal.trim().isEmpty()) {
                if (!codigoPostal.matches("\\d{4}")) {
                    model.put("errorEnvio", "El código postal debe tener 4 dígitos");
                    model.put("envioCalculado", false);
                    model.put("sinCobertura", false);
                } else {
                    EnvioDto envio = servicioDeEnvios.calcularEnvio(codigoPostal);

                    if (envio != null) {
                        this.envioActual = envio;
                        model.put("envio", envio);
                        Double totalConEnvio = total + envio.getCosto();

                        model.put("totalConEnvio", totalConEnvio);
                        model.put("envioCalculado", true);
                        model.put("sinCobertura", false);
                    } else {
                        model.put("envioCalculado", false);
                        model.put("sinCobertura", true);
                        model.put("mensajeEnvio", "No disponemos de envío Andreani para este código postal");
                    }
                }
            }
        } catch (Exception e) {
            model.put("errorEnvio", "Error al calcular envío. Intenta nuevamente.");
            model.put("envioCalculado", false);
            model.put("sinCobertura", false);
        }
        return new ModelAndView("carritoDeCompras", model);
    }

    @GetMapping(path = "/carritoDeCompras/calcular")
    @ResponseBody
    public Map<String, Object> calcularEnvioAjax(@RequestParam String codigoPostal) {
        Map<String, Object> response = new HashMap<>();

        try {
            EnvioDto envio = servicioDeEnvios.calcularEnvio(codigoPostal);

            if (envio != null) {
                //se guardan los valores para pasarselo a procesarCompra y usar en MP despues
                this.envioActual = envio;
                this.codigoPostalActual = codigoPostal;

                response.put("success", true);
                response.put("costo", envio.getCosto());
                response.put("tiempo", envio.getTiempo());
                response.put("destino", envio.getDestino());
                response.put("valorTotal", this.productoService.valorTotal);
            } else {
                this.envioActual = null;
                response.put("success", false);
                response.put("mensaje", "Sin cobertura para este código postal");
            }
        } catch (Exception e) {
            this.envioActual = null;
            response.put("success", false);
            response.put("mensaje", "Error al calcular envío");
        }
        return response;
    }

}
