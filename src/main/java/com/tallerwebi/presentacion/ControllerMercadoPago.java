package com.tallerwebi.presentacion;


import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.common.PhoneRequest;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;

import com.mercadopago.resources.preference.Preference;
import com.tallerwebi.dominio.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/checkout")
public class ControllerMercadoPago {

    //ServicioProductoCarrito sabe los productos que estan en el carrito
    private final ServicioProductoCarritoImpl servicioProductoCarritoImpl;
    private final ServicioPrecios servicioPrecios;


//    @Value("${mercadoPago.accessToken}")
//    private String mercadoPagoAccessToken = "APP_USR-3784718513902185-053117-353d2d4a3d09f6e4ff6bd5750e1b6878-2465514854";

    public ControllerMercadoPago(ServicioProductoCarritoImpl servicioProductoCarritoImpl, ServicioPrecios servicioPrecios) {
        this.servicioProductoCarritoImpl = servicioProductoCarritoImpl;
        this.servicioPrecios = servicioPrecios;
        this.servicioProductoCarritoImpl.init();
    }

    //crearPago se activa cuando alguien hace submit en el formulario del carrito (procesar pago)
    @PostMapping(value = "/create-payment", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public ModelAndView crearPago(HttpServletResponse response,
                                  @RequestParam(value = "metodoDePago", required = false) String metodoDePago,
                                  @RequestParam(value = "costoEnvio", required = false) Double costoEnvio,
                                  @RequestParam(value = "totalOriginal", required = false) Double totalOriginal,
                                  @RequestParam(value = "totalConDescuento", required = false) Double totalConDescuento)
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
                .failure("http://localhost:8080/pagoExitoso")
                .pending("http://localhost:8080/pagoExitoso")
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
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al crear la preferencia de pago. Detalle: " + errorMsg);
            return null;
        } catch (Exception ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error inesperado al procesar el pago.");
            return null;
        }
    }

    private Double getPrecioFinal(PagoRequest pagoRequest, int i) {
        Double precioOriginal = pagoRequest.getProductos().get(i).getPrecio();
        Double factorDescuento = 1.0;

        if (servicioProductoCarritoImpl.valorTotal != null && servicioProductoCarritoImpl.valorTotal > 0) {
            factorDescuento = servicioProductoCarritoImpl.valorTotalConDescuento / servicioProductoCarritoImpl.valorTotal;
        }

        double precioFinal = precioOriginal * factorDescuento;

        // número válido y positivo
        if (Double.isNaN(precioFinal) || precioFinal <= 0) {
            precioFinal = precioOriginal;
        }

        return this.servicioPrecios.conversionDolarAPesoDouble(precioFinal);
    }
}