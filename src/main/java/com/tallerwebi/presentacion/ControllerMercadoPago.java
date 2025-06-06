package com.tallerwebi.presentacion;


import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.common.PhoneRequest;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;

import com.mercadopago.resources.preference.Preference;
import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/checkout")
public class ControllerMercadoPago {
    private static final Logger logger = LoggerFactory.getLogger(ControllerMercadoPago.class);

    //ServicioProductoCarrito sabe los productos que estan en el carrito
    private final ServicioProductoCarrito servicioProductoCarrito;

    @Value("${mercadoPago.accessToken}")
    private String mercadoPagoAccessToken = "APP_USR-3784718513902185-053117-353d2d4a3d09f6e4ff6bd5750e1b6878-2465514854";

    public ControllerMercadoPago(ServicioProductoCarrito servicioProductoCarrito) {
        this.servicioProductoCarrito = servicioProductoCarrito;
        this.servicioProductoCarrito.init();
    }

    //crearPago se activa cuando alguien hace submit en el formulario del carrito (procesar pago)
    @PostMapping(value = "/create-payment", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public ModelAndView crearPago(HttpServletResponse response,
                                  @RequestParam(value = "metodoDePago", required = false) String metodoDePago,
                                  @RequestParam(value = "costoEnvio", required = false) Double costoEnvio,
                                  RedirectAttributes redirectAttributes)
                                    throws IOException {

        // Creo un objeto PagoRequest manualmente
        PagoRequest pagoRequest = new PagoRequest();
        pagoRequest.setMetodoDePago(metodoDePago);
        pagoRequest.setCostoEnvio(costoEnvio);
        // Usa los productos del carrito en lugar de intentar obtenerlos del formulario
        pagoRequest.setProductos(servicioProductoCarrito.getProductos());

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
            double precioFinal = getPrecioFinal(pagoRequest, i);
            PreferenceItemRequest item =
                    PreferenceItemRequest.builder()
                            .title(pagoRequest.getProductos().get(i).getNombre() + " - " + pagoRequest.getProductos().get(i).getDescripcion())
                            .description(pagoRequest.getProductos().get(i).getDescripcion())
                            .quantity(pagoRequest.getProductos().get(i).getCantidad())
                            .currencyId("ARS")
                            .unitPrice(BigDecimal.valueOf(precioFinal))
                            .build();
            items.add(item);
        }

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


//        ESTO LO VOY A USAR CUANDO TENGAMOS USUARIOS LOGUEADOS, TRAEMOS LOS DATOS DEL USUARIO EN SESION PARA CARGAR EL PAYER NECESARIO DE MP (AUNQUE NO ES OBLIGATORIAMENTE NECESARIO PARA MP)
//        Usuario usuario = obtenerUsuarioDeSesion();
//
//        PreferencePayerRequest payer = PreferencePayerRequest.builder()
//                .name(usuario.getNombre())
//                .surname(usuario.getApellido())
//                .email(usuario.getEmail())
//                .phone(PhoneRequest.builder()
//                        .areaCode("11")
//                        .number("11112222")
//                        .build())
//                .identification(IdentificationRequest.builder()
//                        .type("DNI")
//                        .number("12345678")
//                        .build())
//                .build();

        // Creo un comprador para mercado pago con los datos del compradorTest
        PreferencePayerRequest payer = PreferencePayerRequest.builder()
                .name("Test")
                .surname("User")
                .email("test_user_1339781340@testuser.com") // Email ficticio para pruebas
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
                .success("http://localhost:8080/spring/checkout/carritoDeCompras")
                .failure("http://localhost:8080/spring/checkout/carritoDeCompras")
                .pending("http://localhost:8080/spring/checkout/carritoDeCompras")
                .build();

        // Creo la preferencia final que va a ser mandada a mercado pago para la redireccion a su pagina
        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .purpose("wallet_purchase")
                .backUrls(backUrls)
                .payer(payer)
                .externalReference(codigoTransaccion)
                .build();

        // Envio el pedido a mercado pago y me devuelve un link de pago, donde redirige al usuario
        try {
            Preference preference = client.create(preferenceRequest);
            response.sendRedirect(preference.getSandboxInitPoint());
            return null;
        } catch (MPApiException e) {
            String errorMsg = e.getApiResponse() != null ? e.getApiResponse().getContent() : "Sin contenido de error";
            logger.error("MPApiException: Código de estado HTTP = {}, Contenido = {}", e.getStatusCode(), errorMsg, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al crear la preferencia de pago. Detalle: " + errorMsg);
            return null;
        } catch (Exception ex) {
            logger.error("Excepción inesperada: ", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error inesperado al procesar el pago.");
            return null;
        }
    }

    private double getPrecioFinal(PagoRequest pagoRequest, int i) {
        double precioOriginal = pagoRequest.getProductos().get(i).getPrecio();
        double factorDescuento = 1.0;

        if (servicioProductoCarrito.valorTotal != null && servicioProductoCarrito.valorTotal > 0) {
            factorDescuento = servicioProductoCarrito.valorTotalConDescuento / servicioProductoCarrito.valorTotal;
        }

        double precioFinal = precioOriginal * factorDescuento;

        // Asegurar que sea un número válido y positivo
        if (Double.isNaN(precioFinal) || precioFinal <= 0) {
            precioFinal = precioOriginal;
        }
        return precioFinal;
    }
}