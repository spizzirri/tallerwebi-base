package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioCompra;
import com.tallerwebi.dominio.ServicioDeEnviosImpl;
import com.tallerwebi.dominio.ServicioPrecios;
import com.tallerwebi.dominio.ServicioProductoCarritoImpl;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import java.time.LocalDate;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class CarritoController {

    private final ServicioProductoCarritoImpl servicioProductoCarrito;
    private final ServicioDeEnviosImpl servicioDeEnvios;
    private final ServicioPrecios servicioPrecios;
    private final ServicioCompra servicioCompra;


    public String codigoPostalActual;
    public EnvioDto envioActual;

    public CarritoController(ServicioProductoCarritoImpl servicioProductoCarritoImpl, ServicioDeEnviosImpl servicioDeEnvios, ServicioPrecios servicioPrecios, ServicioCompra servicioCompra) {
        this.servicioProductoCarrito = servicioProductoCarritoImpl;
        this.servicioDeEnvios = servicioDeEnvios;
        this.servicioPrecios = servicioPrecios;
        this.servicioCompra = servicioCompra;
        servicioProductoCarritoImpl.init();
    }

    @GetMapping(path = "/carritoDeCompras/index")
    public ModelAndView mostrarVistaCarritoDeCompras(HttpSession session) {
        ModelMap model = new ModelMap();

        List<ProductoCarritoDto> carritoSesion = obtenerCarritoDeSesion(session);
        this.servicioProductoCarrito.setProductos(carritoSesion);
        List<ProductoCarritoDto> productosGuardados = this.servicioProductoCarrito.getProductos();

        for (ProductoCarritoDto producto : productosGuardados) {
            Double totalPorProducto = producto.getPrecio() * producto.getCantidad();
            String totalProductoFormateado = this.servicioPrecios.conversionDolarAPeso(totalPorProducto);
            producto.setPrecioFormateado(totalProductoFormateado);
        }

        model.put("productos", productosGuardados);

        Double total = this.servicioProductoCarrito.calcularValorTotalDeLosProductos();
        String totalFormateado = this.servicioPrecios.conversionDolarAPeso(total);
        model.put("valorTotal", totalFormateado);

        Integer cantidadTotalEnCarrito = this.servicioProductoCarrito.calcularCantidadTotalDeProductos();
        model.put("cantidadEnCarrito", cantidadTotalEnCarrito);

        return new ModelAndView("carritoDeCompras", model);
    }

    @GetMapping(path = "/fragments/fragments")
    public ModelAndView mostrarResumenCarritoDeCompras(HttpSession session) {
        ModelMap model = new ModelMap();

        List<ProductoCarritoDto> carritoSesion = obtenerCarritoDeSesion(session);
        this.servicioProductoCarrito.setProductos(carritoSesion);
        List<ProductoCarritoDto> productos = this.servicioProductoCarrito.getProductos();

        for (ProductoCarritoDto producto : productos) {
            Double totalPorProducto = producto.getPrecio() * producto.getCantidad();
            String totalProductoFormateado = this.servicioPrecios.conversionDolarAPeso(totalPorProducto);
            producto.setPrecioFormateado(totalProductoFormateado);
        }

        model.put("productos", productos);

        Double total = this.servicioProductoCarrito.calcularValorTotalDeLosProductos();
        String totalFormateado = this.servicioPrecios.conversionDolarAPeso(total != null ? total : 0.0);
        model.put("valorTotal", totalFormateado);

        Integer cantidadTotalEnCarrito = this.servicioProductoCarrito.calcularCantidadTotalDeProductos();
        model.put("cantidadEnCarrito", cantidadTotalEnCarrito != null ? cantidadTotalEnCarrito : 0);

        return new ModelAndView("fragments/fragments :: resumenCarrito", model);
    }

    @PostMapping(path = "/carritoDeCompras/eliminarProducto/{id}")
    @ResponseBody
    public Map<String, Object> eliminarProductoDelCarrito(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        List<ProductoCarritoDto> carritoSesion = obtenerCarritoDeSesion(session);
        this.servicioProductoCarrito.setProductos(carritoSesion);

        ProductoCarritoDto productoBuscado = this.servicioProductoCarrito.buscarPorId(id);

        if (productoBuscado != null) {
            this.servicioProductoCarrito.devolverStockAlComponente(id, productoBuscado.getCantidad());
            this.servicioProductoCarrito.getProductos().remove(productoBuscado);
            response.put("eliminado", true);
        } else {
            response.put("eliminado", false);
        }

        session.setAttribute("carritoSesion", this.servicioProductoCarrito.getProductos());
        response.put("productos", this.servicioProductoCarrito.getProductos());

        Double total = this.servicioProductoCarrito.calcularValorTotalDeLosProductos();
        String totalFormateado = this.servicioPrecios.conversionDolarAPeso(total);
        response.put("valorTotal", totalFormateado);

        Integer cantidadTotal = this.servicioProductoCarrito.calcularCantidadTotalDeProductos();
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

        Double valorTotalConDescuento = this.servicioProductoCarrito.calcularDescuento(codigoDescuentoExtraido);

        String valorTotalFormateado = this.servicioPrecios.conversionDolarAPeso(valorTotalConDescuento);

        response.put("mensaje", "Descuento aplicado! Nuevo total: $" + valorTotalFormateado);
        response.put("valorTotal", valorTotalFormateado);

        return response;
    }

    @PostMapping(path = "/carritoDeCompras/agregarMasCantidadDeUnProducto/{id}")
    @ResponseBody
    public Map<String, Object> agregarMasCantidadDeUnProducto(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        List<ProductoCarritoDto> carritoSesion = obtenerCarritoDeSesion(session);
        this.servicioProductoCarrito.setProductos(carritoSesion);

        ProductoCarritoDto productoBuscado = this.servicioProductoCarrito.buscarPorId(id);

        if (productoBuscado != null && this.servicioProductoCarrito.verificarStock(id)) {
            this.servicioProductoCarrito.descontarStockAlComponente(id, 1);
            productoBuscado.setCantidad(productoBuscado.getCantidad() + 1);

            response.put("cantidad", productoBuscado.getCantidad());

            Double totalPorProducto = productoBuscado.getCantidad() * productoBuscado.getPrecio();
            String totalFormateadoPorProducto = this.servicioPrecios.conversionDolarAPeso(totalPorProducto);
            response.put("precioTotalDelProducto", totalFormateadoPorProducto);

            Double totalGeneral = this.servicioProductoCarrito.calcularValorTotalDeLosProductos();
            String totalGeneralFormateado = this.servicioPrecios.conversionDolarAPeso(totalGeneral);
            response.put("valorTotal", totalGeneralFormateado);

            response.put("cantidadEnCarrito", this.servicioProductoCarrito.calcularCantidadTotalDeProductos());
        } else {
            response.put("success", false);
            response.put("mensaje", "No hay stock suficiente!");
        }

        session.setAttribute("carritoSesion", this.servicioProductoCarrito.getProductos());
        return response;
    }

    @PostMapping("/carritoDeCompras/restarCantidadDeUnProducto/{id}")
    @ResponseBody
    public Map<String, Object> restarCantidadDeUnProducto(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        List<ProductoCarritoDto> carritoSesion = obtenerCarritoDeSesion(session);
        this.servicioProductoCarrito.setProductos(carritoSesion);

        ProductoCarritoDto productoBuscado = this.servicioProductoCarrito.buscarPorId(id);

        if (productoBuscado != null && productoBuscado.getCantidad() > 1) {
            productoBuscado.setCantidad(productoBuscado.getCantidad() - 1);
            this.servicioProductoCarrito.devolverStockAlComponente(id, 1);

            response.put("cantidad", productoBuscado.getCantidad());

            Double totalPorProducto = productoBuscado.getCantidad() * productoBuscado.getPrecio();
            String totalFormateadoPorProducto = this.servicioPrecios.conversionDolarAPeso(totalPorProducto);
            response.put("precioTotalDelProducto", totalFormateadoPorProducto);

            response.put("eliminado", false);

            Double totalGeneral = this.servicioProductoCarrito.calcularValorTotalDeLosProductos();
            String totalGeneralFormateado = this.servicioPrecios.conversionDolarAPeso(totalGeneral);
            response.put("valorTotal", totalGeneralFormateado);

            response.put("cantidadEnCarrito", this.servicioProductoCarrito.calcularCantidadTotalDeProductos());

        } else if (productoBuscado != null) {
            this.servicioProductoCarrito.getProductos().remove(productoBuscado);
            this.servicioProductoCarrito.devolverStockAlComponente(id, 1);

            response.put("eliminado", true);

            Double totalGeneral = this.servicioProductoCarrito.calcularValorTotalDeLosProductos();
            String totalGeneralFormateado = this.servicioPrecios.conversionDolarAPeso(totalGeneral);
            response.put("valorTotal", totalGeneralFormateado);

            response.put("cantidadEnCarrito", this.servicioProductoCarrito.calcularCantidadTotalDeProductos());
        }

        session.setAttribute("carritoSesion", this.servicioProductoCarrito.getProductos());
        return response;
    }

    @PostMapping(path = "/carritoDeCompras/formularioPago")
    @ResponseBody
    public Map<String, Object> procesarCompra(@RequestParam(value = "metodoPago") String metodoDePago, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        // para obtener el usuario que esta logueado
        UsuarioDto usuarioLogueado = (UsuarioDto) session.getAttribute("usuario");

        if (metodoDePago == null || metodoDePago.isEmpty()) {
            response.put("success", false);
            response.put("error", "Debes seleccionar un metodo de pago");
            return response;
        }

        if(usuarioLogueado == null) {
            response.put("success", false);
            response.put("error", "Debes iniciar sesion");
            response.put("redirect", "/login");
//            response.put("redirect", "/login?redirectUrl=/carritoDeCompras/formularioPago");
            return response;
        }

        if (this.envioActual == null || this.codigoPostalActual == null) {
            response.put("success", false);
            response.put("error", "Debes agregar un codigo postal");
            return response;
        }

        if (metodoDePago.equalsIgnoreCase("tarjetaCredito")) {
            response.put("success", true);
            response.put("redirect", "/tarjetaDeCredito");
            return response;
        }
        if ("mercadoPago".equalsIgnoreCase(metodoDePago)) {
            response.put("success", true);
            response.put("metodoPago", "mercadoPago");
            response.put("costoEnvio", envioActual.getCosto());
            return response;
        } else {
            response.put("success", false);
            response.put("error", "Método de pago no soportado: " + metodoDePago);
        }
        return response;
    }


    @GetMapping(path = "/carritoDeCompras/calcularEnvio")
    @ResponseBody
    public Map<String, Object> calcularEnvioAjax(@RequestParam String codigoPostal, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        List<ProductoCarritoDto> carritoSesion = obtenerCarritoDeSesion(session);
        this.servicioProductoCarrito.setProductos(carritoSesion);

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
                response.put("valorTotal", this.servicioProductoCarrito.valorTotal);
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
        Integer cantidadTotal = this.servicioProductoCarrito.calcularCantidadTotalDeProductos();
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
            if (!servicioProductoCarrito.verificarStock(componenteId)) {
                response.put("success", false);
                response.put("mensaje", "Stock insuficiente");
            } else {
                List<ProductoCarritoDto> carritoSesion = obtenerCarritoDeSesion(session);

                servicioProductoCarrito.setProductos(carritoSesion);

                ProductoCarritoDto existente = servicioProductoCarrito.buscarPorId(componenteId);
                servicioProductoCarrito.agregarProducto(componenteId, cantidad);

                if (existente != null) {
                    int cantidadFinal = existente.getCantidad();
                    response.put("mensaje", "Producto actualizado! Ahora tiene " + cantidadFinal + " unidades en el carrito.");
                } else {
                    response.put("mensaje", "Producto agregado al carrito!");
                }

                session.setAttribute("carritoSesion", servicioProductoCarrito.getProductos());
                response.put("success", true);
            }

            Integer cantidadTotal = servicioProductoCarrito.calcularCantidadTotalDeProductos();
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
