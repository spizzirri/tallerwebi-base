package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioDeEnviosImpl;
import com.tallerwebi.dominio.ServicioPrecios;
import com.tallerwebi.dominio.ServicioProductoCarritoImpl;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
public class CarritoController {

    private final ServicioProductoCarritoImpl productoService;
    private final ServicioDeEnviosImpl servicioDeEnvios;
    private final ServicioPrecios servicioPrecios;

    public String codigoPostalActual;
    public EnvioDto envioActual;

    public CarritoController(ServicioProductoCarritoImpl servicioProductoCarritoImpl, ServicioDeEnviosImpl servicioDeEnvios, ServicioPrecios servicioPrecios) {
        this.productoService = servicioProductoCarritoImpl;
        this.servicioDeEnvios = servicioDeEnvios;
        this.servicioPrecios = servicioPrecios;
        servicioProductoCarritoImpl.init();
    }

    @GetMapping(path = "/carritoDeCompras/index")
    public ModelAndView mostrarVistaCarritoDeCompras(HttpSession session) {
        ModelMap model = new ModelMap();

        List<ProductoCarritoDto> carritoSesion = obtenerCarritoDeSesion(session);
        this.productoService.setProductos(carritoSesion);
        List<ProductoCarritoDto> productosGuardados = this.productoService.getProductos();

        for (ProductoCarritoDto producto : productosGuardados) {
            Double totalPorProducto = producto.getPrecio() * producto.getCantidad();
            String totalProductoFormateado = this.servicioPrecios.conversionDolarAPeso(totalPorProducto);
            producto.setPrecioFormateado(totalProductoFormateado);
        }

        model.put("productos", productosGuardados);

        Double total = this.productoService.calcularValorTotalDeLosProductos();
        String totalFormateado = this.servicioPrecios.conversionDolarAPeso(total);
        model.put("valorTotal", totalFormateado);

        Integer cantidadTotalEnCarrito = this.productoService.calcularCantidadTotalDeProductos();
        model.put("cantidadEnCarrito", cantidadTotalEnCarrito);

        return new ModelAndView("carritoDeCompras", model);
    }

    @GetMapping(path = "/fragments/fragments")
    public ModelAndView mostrarResumenCarritoDeCompras(HttpSession session) {
        ModelMap model = new ModelMap();

        List<ProductoCarritoDto> carritoSesion = obtenerCarritoDeSesion(session);
        this.productoService.setProductos(carritoSesion);
        List<ProductoCarritoDto> productos = this.productoService.getProductos();

        for (ProductoCarritoDto producto : productos) {
            Double totalPorProducto = producto.getPrecio() * producto.getCantidad();
            String totalProductoFormateado = this.servicioPrecios.conversionDolarAPeso(totalPorProducto);
            producto.setPrecioFormateado(totalProductoFormateado);
        }

        model.put("productos", productos);

        Double total = this.productoService.calcularValorTotalDeLosProductos();
        String totalFormateado = this.servicioPrecios.conversionDolarAPeso(total != null ? total : 0.0);
        model.put("valorTotal", totalFormateado);

        Integer cantidadTotalEnCarrito = this.productoService.calcularCantidadTotalDeProductos();
        model.put("cantidadEnCarrito", cantidadTotalEnCarrito != null ? cantidadTotalEnCarrito : 0);

        return new ModelAndView("fragments/fragments :: resumenCarrito", model);
    }

    @PostMapping(path = "/carritoDeCompras/eliminarProducto/{id}")
    @ResponseBody
    public Map<String, Object> eliminarProductoDelCarrito(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        List<ProductoCarritoDto> carritoSesion = obtenerCarritoDeSesion(session);
        this.productoService.setProductos(carritoSesion);

        ProductoCarritoDto productoBuscado = this.productoService.buscarPorId(id);

        if (productoBuscado != null) {
            this.productoService.devolverStockAlComponente(id, productoBuscado.getCantidad());
            this.productoService.getProductos().remove(productoBuscado);
            response.put("eliminado", true);
        } else {
            response.put("eliminado", false);
        }

        session.setAttribute("carritoSesion", this.productoService.getProductos());
        response.put("productos", this.productoService.getProductos());

        Double total = this.productoService.calcularValorTotalDeLosProductos();
        String totalFormateado = this.servicioPrecios.conversionDolarAPeso(total);
        response.put("valorTotal", totalFormateado);

        Integer cantidadTotal = this.productoService.calcularCantidadTotalDeProductos();
        response.put("cantidadEnCarrito", cantidadTotal);

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
    public Map<String, Object> calcularValorTotalDeLosProductosConDescuento
            (@RequestBody Map<String, String> codigoDescuentoMap) {
        String codigoDescuento = codigoDescuentoMap.get("codigoInput");

        Integer codigoDescuentoExtraido = extraerPorcentajeDesdeCodigoDeDescuento(codigoDescuento);
        Map<String, Object> response = new HashMap<>();

        if (codigoDescuentoExtraido == null || !(codigoDescuentoExtraido == 5 || codigoDescuentoExtraido == 10 || codigoDescuentoExtraido == 15)) {
            response.put("mensajeDescuento", "Codigo de descuento invalido!");
            return response;
        }

        Double valorTotalConDescuento = this.productoService.calcularDescuento(codigoDescuentoExtraido);

        String valorTotalFormateado = this.servicioPrecios.conversionDolarAPeso(valorTotalConDescuento);

        response.put("mensaje", "Descuento aplicado! Nuevo total: $" + valorTotalFormateado);
        response.put("valorTotal", valorTotalFormateado); // Ahora envías el String formateado

        return response;
    }

    @PostMapping(path = "/carritoDeCompras/agregarMasCantidadDeUnProducto/{id}")
    @ResponseBody
    public Map<String, Object> agregarMasCantidadDeUnProducto(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        List<ProductoCarritoDto> carritoSesion = obtenerCarritoDeSesion(session);
        this.productoService.setProductos(carritoSesion);

        ProductoCarritoDto productoBuscado = this.productoService.buscarPorId(id);

        if (productoBuscado != null && this.productoService.verificarStock(id)) {
            this.productoService.descontarStockAlComponente(id, 1);
            productoBuscado.setCantidad(productoBuscado.getCantidad() + 1);

            response.put("cantidad", productoBuscado.getCantidad());

            Double totalPorProducto = productoBuscado.getCantidad() * productoBuscado.getPrecio();
            String totalFormateadoPorProducto = this.servicioPrecios.conversionDolarAPeso(totalPorProducto);
            response.put("precioTotalDelProducto", totalFormateadoPorProducto);

            Double totalGeneral = this.productoService.calcularValorTotalDeLosProductos();
            String totalGeneralFormateado = this.servicioPrecios.conversionDolarAPeso(totalGeneral);
            response.put("valorTotal", totalGeneralFormateado);

            response.put("cantidadEnCarrito", this.productoService.calcularCantidadTotalDeProductos());
        } else {
            response.put("success", false);
            response.put("mensaje", "No hay stock suficiente!");
        }

        session.setAttribute("carritoSesion", this.productoService.getProductos());
        return response;
    }

    @PostMapping("/carritoDeCompras/restarCantidadDeUnProducto/{id}")
    @ResponseBody
    public Map<String, Object> restarCantidadDeUnProducto(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        List<ProductoCarritoDto> carritoSesion = obtenerCarritoDeSesion(session);
        this.productoService.setProductos(carritoSesion);

        ProductoCarritoDto productoBuscado = this.productoService.buscarPorId(id);

        if (productoBuscado != null && productoBuscado.getCantidad() > 1) {
            productoBuscado.setCantidad(productoBuscado.getCantidad() - 1);
            this.productoService.devolverStockAlComponente(id, 1);

            response.put("cantidad", productoBuscado.getCantidad());

            Double totalPorProducto = productoBuscado.getCantidad() * productoBuscado.getPrecio();
            String totalFormateadoPorProducto = this.servicioPrecios.conversionDolarAPeso(totalPorProducto);
            response.put("precioTotalDelProducto", totalFormateadoPorProducto);

            response.put("eliminado", false);

            Double totalGeneral = this.productoService.calcularValorTotalDeLosProductos();
            String totalGeneralFormateado = this.servicioPrecios.conversionDolarAPeso(totalGeneral);
            response.put("valorTotal", totalGeneralFormateado);

            response.put("cantidadEnCarrito", this.productoService.calcularCantidadTotalDeProductos());

        } else if (productoBuscado != null) {
            this.productoService.getProductos().remove(productoBuscado);
            this.productoService.devolverStockAlComponente(id, 1);

            response.put("eliminado", true);

            Double totalGeneral = this.productoService.calcularValorTotalDeLosProductos();
            String totalGeneralFormateado = this.servicioPrecios.conversionDolarAPeso(totalGeneral);
            response.put("valorTotal", totalGeneralFormateado);

            response.put("cantidadEnCarrito", this.productoService.calcularCantidadTotalDeProductos());
        }

        session.setAttribute("carritoSesion", this.productoService.getProductos());
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
        if (metodoDePago.equalsIgnoreCase("tarjetaCredito")) {
            response.put("success", true);
            response.put("redirect", "/tarjetaDeCredito");
            return response;
        }
        if ("mercadoPago".equalsIgnoreCase(metodoDePago)) {
            if (this.envioActual == null || this.codigoPostalActual == null) {
                response.put("success", false);
                response.put("error", "Debes agregar un codigo postal");
                return response;
            }
            response.put("success", true);
            response.put("metodoPago", "mercadoPago");
            response.put("costoEnvio", envioActual.getCosto());
        } else {
            response.put("success", false);
            response.put("error", "Método de pago no soportado: " + metodoDePago);
        }
        return response;
    }

//    @PostMapping(path = "/carritoDeCompras/calcularEnvio")
//    public ModelAndView calcularEnvio(@RequestParam(value = "codigoPostal", required = false) String codigoPostal, HttpSession session) {
//
//        ModelMap model = new ModelMap();
//        List<ProductoCarritoDto> carritoSesion = obtenerCarritoDeSesion(session);
//        this.productoService.setProductos(carritoSesion);
//
//        this.codigoPostalActual = codigoPostal;
//
//        model.put("productos", this.productoService.getProductos());
//
//        Double total = this.productoService.calcularValorTotalDeLosProductos();
//
//        model.put("valorTotal", total);
//        model.put("codigoPostal", codigoPostal);
//
//        try {
//            if (codigoPostal != null && !codigoPostal.trim().isEmpty()) {
//                if (!codigoPostal.matches("\\d{4}")) {
//                    model.put("errorEnvio", "El código postal debe tener 4 dígitos");
//                    model.put("envioCalculado", false);
//                    model.put("sinCobertura", false);
//                } else {
//                    EnvioDto envio = servicioDeEnvios.calcularEnvio(codigoPostal);
//
//                    if (envio != null) {
//                        this.envioActual = envio;
//                        model.put("envio", envio);
//                        Double totalConEnvio = total + envio.getCosto();
//
//                        model.put("totalConEnvio", totalConEnvio);
//                        model.put("envioCalculado", true);
//                        model.put("sinCobertura", false);
//                    } else {
//                        model.put("envioCalculado", false);
//                        model.put("sinCobertura", true);
//                        model.put("mensajeEnvio", "No disponemos de envío Andreani para este código postal");
//                    }
//                }
//            }
//        } catch (Exception e) {
//            model.put("errorEnvio", "Error al calcular envío. Intenta nuevamente.");
//            model.put("envioCalculado", false);
//            model.put("sinCobertura", false);
//        }
//        return new ModelAndView("carritoDeCompras", model);
//    }

    @GetMapping(path = "/carritoDeCompras/calcularEnvio")
    @ResponseBody
    public Map<String, Object> calcularEnvioAjax(@RequestParam String codigoPostal, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        List<ProductoCarritoDto> carritoSesion = obtenerCarritoDeSesion(session);
        this.productoService.setProductos(carritoSesion);

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

    @GetMapping(path = "/carritoDeCompras/cantidad")
    @ResponseBody
    public Map<String, Object> obtenerCantidadCarrito() {
        Map<String, Object> response = new HashMap<>();
        Integer cantidadTotal = this.productoService.calcularCantidadTotalDeProductos();
        response.put("cantidadEnCarrito", cantidadTotal);
        return response;
    }

    @GetMapping("/agregarAlCarrito/{componenteId}/{cantidad}")
    @ResponseBody
    public Map<String, Object> agregarProductoAlCarrito(
            @PathVariable Long componenteId,
            @PathVariable Integer cantidad,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (!productoService.verificarStock(componenteId)) {
                response.put("success", false);
                response.put("mensaje", "Stock insuficiente");
            } else {
                List<ProductoCarritoDto> carritoSesion = obtenerCarritoDeSesion(session);

                productoService.setProductos(carritoSesion);

                ProductoCarritoDto existente = productoService.buscarPorId(componenteId);
                productoService.agregarProducto(componenteId, cantidad);

                if (existente != null) {
                    int cantidadFinal = existente.getCantidad();
                    response.put("mensaje", "Producto actualizado! Ahora tiene " + cantidadFinal + " unidades en el carrito.");
                } else {
                    response.put("mensaje", "Producto agregado al carrito!");
                }

                session.setAttribute("carritoSesion", productoService.getProductos());
                response.put("success", true);
            }

            Integer cantidadTotal = productoService.calcularCantidadTotalDeProductos();
            response.put("cantidadEnCarrito", cantidadTotal != null ? cantidadTotal : 0);

        } catch (Exception e) {
            response.put("success", false);
            response.put("mensaje", "Error al agregar producto al carrito!");
            response.put("cantidadEnCarrito", 0);
        }
        return response;
    }

    private List<ProductoCarritoDto> obtenerCarritoDeSesion(HttpSession session) {
        List<ProductoCarritoDto> carritoSesion = (List<ProductoCarritoDto>) session.getAttribute("carritoSesion");
        if (carritoSesion == null) {
            carritoSesion = new ArrayList<>();
            session.setAttribute("carritoSesion", carritoSesion);
        }
        return carritoSesion;
    }
}
