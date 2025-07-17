package com.tallerwebi.presentacion;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.common.PhoneRequest;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;

import com.mercadopago.resources.preference.Preference;
import com.tallerwebi.dominio.*;
import com.tallerwebi.presentacion.dto.ProductoCarritoArmadoDto;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/checkout")
public class ControllerMercadoPago {

    //ServicioProductoCarrito sabe los productos que estan en el carrito
    private ServicioProductoCarritoImpl servicioProductoCarritoImpl;
    private ServicioPrecios servicioPrecios;
    private ServicioProductoCarritoImpl servicioProductoCarrito;
    private ServicioCompra servicioCompra;

    public ControllerMercadoPago(ServicioProductoCarritoImpl servicioProductoCarritoImpl, ServicioPrecios servicioPrecios, ServicioProductoCarritoImpl servicioProductoCarrito, ServicioCompra servicioCompra) {
        this.servicioProductoCarritoImpl = servicioProductoCarritoImpl;
        this.servicioPrecios = servicioPrecios;
        this.servicioProductoCarrito = servicioProductoCarrito;
        this.servicioCompra = servicioCompra;
        this.servicioProductoCarritoImpl.init();
    }

    //crearPago se activa cuando alguien hace submit en el formulario del carrito (procesar pago)
    @PostMapping(value = "/create-payment", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public ModelAndView crearPago(HttpServletResponse response,
                                  @RequestParam(value = "metodoDePago", required = false) String metodoDePago,
                                  @RequestParam(value = "costoEnvio", required = false) Double costoEnvio,
                                  HttpSession session)
                                    throws IOException {

        // Creo un objeto PagoRequest manualmente
        PagoRequest pagoRequest = new PagoRequest();
        pagoRequest.setMetodoDePago(metodoDePago);
        pagoRequest.setCostoEnvio(costoEnvio);
        // Usa los productos del carrito en lugar de intentar obtenerlos del formulario
        pagoRequest.setProductos(servicioProductoCarritoImpl.getProductos());

        // Crear listas para almacenar cantidades e IDs de productos
        List<Integer> cantidades = new ArrayList<>();
        List<Long> idsProductos = new ArrayList<>();

        // Extraer la información de la lista de productos
        for (ProductoCarritoDto producto : pagoRequest.getProductos()) {
            cantidades.add(producto.getCantidad());
            idsProductos.add(producto.getId());
        }

        //configuracion de mercado pago para usar la "app" de prueba creada con el usuario vendedorTest
        MercadoPagoConfig.setAccessToken("APP_USR-3784718513902185-053117-353d2d4a3d09f6e4ff6bd5750e1b6878-2465514854");

        // Creo un objeto de preferencia
        PreferenceClient client = new PreferenceClient();

        // Creo un ítem con los productos en la preferencia de mercado pago (de forma que lo entienda su sistema)
        List<PreferenceItemRequest> items = new ArrayList<>();
        for (int i = 0; i < pagoRequest.getProductos().size(); i++) {
            Double precioFinal = getPrecioFinal(pagoRequest, i);

            PreferenceItemRequest item =
                    PreferenceItemRequest.builder()
                            .title(pagoRequest.getProductos().get(i).getNombre())
                            .quantity(pagoRequest.getProductos().get(i).getCantidad())
                            .currencyId("ARS")
                            .unitPrice(BigDecimal.valueOf(precioFinal))
                            .build();
            items.add(item);
        }

        // Creo un item para pasarle a MP el costo del envio por separado
        if (pagoRequest.getCostoEnvio() != null && pagoRequest.getCostoEnvio() > 0) {
            PreferenceItemRequest itemEnvio =
                    PreferenceItemRequest.builder()
                            .title("Envío")
                            .quantity(1)
                            .currencyId("ARS")
                            .unitPrice(BigDecimal.valueOf(pagoRequest.getCostoEnvio()))
                            .build();
            items.add(itemEnvio);
        }

        // Creo un comprador para mercado pago con los datos del compradorTest
        PreferencePayerRequest payer = PreferencePayerRequest.builder()
                .name("Test")
                .surname("User")
                .email("test_user_46542185@testuser.com") // Email ficticio para pruebas
                .phone(PhoneRequest.builder()
                        .areaCode("11")
                        .number("11112222")
                        .build())
                .identification(IdentificationRequest.builder()
                        .type("DNI")
                        .number("12345678")
                        .build())
                .build();

        String codigoTransaccion = UUID.randomUUID().toString();

        //Donde redireccionar depues de hacer el pago (arreglarlo para que me devuelva a la vista de nuestra app)
        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()

                .success("http://localhost:8080/pagoExitoso")
                .failure("http://localhost:8080/checkout/pagoExitoso")
                .pending("http://localhost:8080/checkout/pagoExitoso")
                .build();

//        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
//                .success("https://www.google.com")
//                .failure("https://www.google.com")
//                .pending("https://www.google.com")
//                .build();

        // Creo la preferencia final que va a ser mandada a mercado pago para la redireccion a su pagina
        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .purpose("wallet_purchase")
                .backUrls(backUrls)
                .payer(payer)
//                .autoReturn("approved")
                .build();

        // Envio el pedido a mercado pago y me devuelve un link de pago, donde redirige al usuario
        try {
            Preference preference = client.create(preferenceRequest);

            UsuarioDto usuarioLogueado = (UsuarioDto) session.getAttribute("usuario");

            List<ProductoCarritoDto> carritoSesion = obtenerCarritoDeSesion(session);

            Double totalCompraEnDolares = this.servicioProductoCarrito.calcularValorTotalDeLosProductos();
            Double totalCompraEnPesos = this.servicioPrecios.conversionDolarAPesoDouble(totalCompraEnDolares);

            CompraDto compraDto = new CompraDto();
            compraDto.setFecha(LocalDateTime.now());
            compraDto.setMetodoDePago(metodoDePago);
            compraDto.setTotal(totalCompraEnPesos);
            compraDto.setProductosComprados(convertirACompraComponenteDto(carritoSesion));
            compraDto.setFormaEntrega((String) session.getAttribute("formaEntrega"));
            compraDto.setCostoDeEnvio((Double) session.getAttribute("costo"));
            compraDto.setMoneda("pesos");
            compraDto.setTotalDolar(totalCompraEnDolares);
            compraDto.setValorConDescuento((String) session.getAttribute("totalConDescuento"));
            compraDto.setTotalConDescuentoDolar((Double) session.getAttribute("totalConDescuentoNoFormateado"));

            servicioCompra.guardarCompraConUsuarioLogueado(compraDto, usuarioLogueado, session);
            response.sendRedirect(preference.getSandboxInitPoint());
            servicioProductoCarritoImpl.limpiarCarrito();

            session.removeAttribute("totalConDescuento");
            session.removeAttribute("totalConDescuentoNoFormateado");


            return null;
        } catch (MPApiException e) {
            String errorMsg = e.getApiResponse() != null ? e.getApiResponse().getContent() : "Sin contenido de error";
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al crear la preferencia de pago. Detalle: " + errorMsg);
            return null;
        }  catch (Exception ex) {
            ex.printStackTrace(); // Esto imprimirá el error en la consola
            try {
                System.out.println("Error específico: " + ex.getMessage());
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error inesperado al procesar el pago: " + ex.getMessage());

            return null;
        }
    }

//    @GetMapping("/pagoExitoso")
//    public ModelAndView procesarPagoExitoso(@RequestParam(value = "status", required = false) String status,
//                                            HttpSession session) {
//
//        String metodoDePagoPendiente = (String) session.getAttribute("metodoDePagoPendiente");
//        UsuarioDto usuarioCompraPendiente = (UsuarioDto) session.getAttribute("usuarioCompraPendiente");
//
//        if (metodoDePagoPendiente != null && usuarioCompraPendiente != null) {
//            try {
//                List<ProductoCarritoDto> carritoSesion = obtenerCarritoDeSesion(session);
//
//                Double totalCompraEnDolares = this.servicioProductoCarrito.calcularValorTotalDeLosProductos();
//                Double totalCompraEnPesos = this.servicioPrecios.conversionDolarAPesoDouble(totalCompraEnDolares);
//
//                CompraDto compraDto = new CompraDto();
//                compraDto.setFecha(LocalDate.now());
//                compraDto.setMetodoDePago(metodoDePagoPendiente);
//                compraDto.setTotal(totalCompraEnPesos);
//                compraDto.setProductosComprados(convertirACompraComponenteDto(carritoSesion));
//
//                servicioCompra.guardarCompraConUsuarioLogueado(compraDto, usuarioCompraPendiente, session);
//
//                servicioProductoCarritoImpl.limpiarCarrito();
//
//                session.removeAttribute("metodoDePagoPendiente");
//                session.removeAttribute("usuarioCompraPendiente");
//            } catch (Exception e) {
//                return new ModelAndView("redirect:/pagoFallido?error=procesamiento");
//            }
//        }
//
//        return new ModelAndView("redirect:/pagoExitoso");
//    }

    private List<ProductoCarritoDto> obtenerCarritoDeSesion(HttpSession session) {
        List<ProductoCarritoDto> carritoSesion = (List<ProductoCarritoDto>) session.getAttribute("carritoSesion");
        if (carritoSesion == null) {
            carritoSesion = new ArrayList<>();
            session.setAttribute("carritoSesion", carritoSesion);
        }
        return carritoSesion;
    }

    private List<CompraComponenteDto> convertirACompraComponenteDto(List<ProductoCarritoDto> productosCarritoDto) {
        return productosCarritoDto.stream().map(productosCarrito -> {
            CompraComponenteDto compraComponenteDto = new CompraComponenteDto();
            compraComponenteDto.setCantidad(productosCarrito.getCantidad());
            Double precioEnPesos = this.servicioPrecios.conversionDolarAPesoDouble(productosCarrito.getPrecio() * productosCarrito.getCantidad());
            compraComponenteDto.setPrecioDolar(productosCarrito.getPrecio() * productosCarrito.getCantidad());
            compraComponenteDto.setPrecioUnitario(precioEnPesos);
            compraComponenteDto.setId(productosCarrito.getId());
            if (productosCarrito instanceof ProductoCarritoArmadoDto){
                ProductoCarritoArmadoDto productoCarritoArmadoDto =  (ProductoCarritoArmadoDto) productosCarrito;
                compraComponenteDto.setEsArmado(true);
                compraComponenteDto.setNumeroDeArmado(productoCarritoArmadoDto.getNumeroDeArmadoAlQuePertenece());
            } else {
                compraComponenteDto.setEsArmado(false);
            }
            return compraComponenteDto;
        }).collect(Collectors.toList());
    }

    private Double getPrecioFinal(PagoRequest pagoRequest, int i) {
        Double precioOriginal = pagoRequest.getProductos().get(i).getPrecio();
        Double factorDescuento = 1.0;

        if (servicioProductoCarritoImpl.valorTotal != null && servicioProductoCarritoImpl.valorTotal > 0) {
            factorDescuento = servicioProductoCarritoImpl.valorTotalConDescuento / servicioProductoCarritoImpl.valorTotal;
        }

        double precioFinal = precioOriginal * factorDescuento;

        if (Double.isNaN(precioFinal) || precioFinal <= 0) {
            precioFinal = precioOriginal;
        }

        return this.servicioPrecios.conversionDolarAPesoDouble(precioFinal);
    }
}