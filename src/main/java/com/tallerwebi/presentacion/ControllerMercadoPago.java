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

    private final ServicioProductoCarrito servicioProductoCarrito;

    @Value("${mercadoPago.accessToken}")
    private String mercadoPagoAccessToken = "APP_USR-3784718513902185-053117-353d2d4a3d09f6e4ff6bd5750e1b6878-2465514854";

    public ControllerMercadoPago(ServicioProductoCarrito servicioProductoCarrito) {
        this.servicioProductoCarrito = servicioProductoCarrito;
        this.servicioProductoCarrito.init();
    }

    @PostMapping(value = "/create-payment", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public ModelAndView crearPago(HttpServletResponse response,
                                  @RequestParam(value = "metodoDePago", required = false) String metodoDePago,
                                  @RequestParam Map<String, String> allParams,
                                  RedirectAttributes redirectAttributes)
            throws MPException, MPApiException, IOException, UsuarioExistente, UsuarioExistente {
        // Crea un objeto PagoRequest manualmente
        PagoRequest pagoRequest = new PagoRequest();
        pagoRequest.setMetodoDePago(metodoDePago);
        // Crea y configura el objeto FormularioDePagoDTO
        FormularioDePagoDTO formularioPagoDTO = new FormularioDePagoDTO();
        formularioPagoDTO.setNombre(allParams.get("formularioPagoDTO.nombre"));
        formularioPagoDTO.setApellido(allParams.get("formularioPagoDTO.apellido"));
        formularioPagoDTO.setEmail(allParams.get("formularioPagoDTO.email"));
        formularioPagoDTO.setTelefono(allParams.get("formularioPagoDTO.telefono"));
        formularioPagoDTO.setDni(allParams.get("formularioPagoDTO.dni"));
        formularioPagoDTO.setCodigoDescuento(allParams.get("formularioPagoDTO.codigoDescuento"));
        pagoRequest.setFormularioPagoDTO(formularioPagoDTO);
        // Usa los productos del carrito en lugar de intentar obtenerlos del formulario
        pagoRequest.setProductos(servicioProductoCarrito.getProductos());

        logger.info("PagoRequest recibido: {}", pagoRequest);
        logger.info("FormularioPagoDTO: {}", pagoRequest.getFormularioPagoDTO());
        logger.info("Método de pago: {}", pagoRequest.getMetodoDePago());
        logger.info("Productos: {}", pagoRequest.getProductos());

        if (metodoDePago == null || metodoDePago.isEmpty()) {
            logger.warn("Error en procesamiento de compra: método de pago no seleccionado");
        } else {

            logger.info("Método de pago seleccionado correctamente: {}", metodoDePago);
        }

        // Crear listas para almacenar cantidades e IDs de productos
        List<Integer> cantidades = new ArrayList<>();
        List<Long> idsProductos = new ArrayList<>();

        // Extraer la información de la lista de productos
        for (ProductoCarritoDto producto : pagoRequest.getProductos()) {
            cantidades.add(producto.getCantidad());
            idsProductos.add(producto.getId());
        }

        MercadoPagoConfig.setAccessToken("APP_USR-3784718513902185-053117-353d2d4a3d09f6e4ff6bd5750e1b6878-2465514854");

        Usuario user = new Usuario(
                formularioPagoDTO.getEmail(),
                "",
                formularioPagoDTO.getNombre(),
                formularioPagoDTO.getApellido(),
                formularioPagoDTO.getTelefono(),
                formularioPagoDTO.getDni());
        Double totalCarrito = servicioProductoCarrito.calcularValorTotalDeLosProductos();

// Calcular total con descuento si hay código
        String codigoDescuento = formularioPagoDTO.getCodigoDescuento();
        if (codigoDescuento != null && !codigoDescuento.isEmpty()) {
            // Usar tu metodo existente del CarritoController para extraer porcentaje
            String numeroExtraido = codigoDescuento.replaceAll("^.*?(\\d+)$", "$1");
            try {
                Integer porcentaje = Integer.parseInt(numeroExtraido);
                if (porcentaje == 5 || porcentaje == 10 || porcentaje == 15) {
                    // Aplicar descuento usando tu servicio existente
                    servicioProductoCarrito.calcularDescuento(porcentaje);
                    logger.info("Descuento del {}% aplicado en MercadoPago", porcentaje);
                }
            } catch (NumberFormatException e) {
                logger.warn("Error al procesar código de descuento: {}", codigoDescuento);
            }
        }
        // Crea un objeto de preferencia
        PreferenceClient client = new PreferenceClient();


        // Crea un ítem en la preferencia
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

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                //modificar para que lleve a la vista correcta (todavia no esta creada)
                .success("http://localhost:8080/spring/checkout/carritoDeCompras/compraFinalizada")
                .failure("http://localhost:8080/spring/checkout/carritoDeCompras/compraFinalizada?" + codigoTransaccion)
                .pending("https://www.pending.com")
                .build();

        PreferenceRequest preferenceRequest =
                PreferenceRequest.builder()
                        .items(items)
                        .purpose("wallet_purchase")
                        .backUrls(backUrls)
                        .payer(payer)
                        .build();

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

    @GetMapping("/carritoDeCompras/compraFinalizada")
    public ModelAndView mostrarVistaCompraFinalizada(
            @RequestParam("codigoTransaccion") String codigoTransaccion,
            @RequestParam("status") String status) {
        ModelMap modelo = new ModelMap();
        // Agregar el código de transacción al modelo
        modelo.put("codigoTransaccion", codigoTransaccion);
        modelo.put("status", status);

        if ("approved".equals(status)) {
            return new ModelAndView("compraRealizada", modelo);
        }
        return new ModelAndView("compraRealizada", modelo);
    }


}