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

    private final ProductoService productoService;

    @Value("${mercadoPago.accessToken}")
    private String mercadoPagoAccessToken = "TEST-6775356251432010-052721-6aa60902aaef96aebd58e3b7112d3432-477764900";

    public ControllerMercadoPago(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping(value = "/create-payment",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
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
         pagoRequest.setProductos(productoService.getProductos());

        logger.info("PagoRequest recibido: {}", pagoRequest);
        logger.info("FormularioPagoDTO: {}", pagoRequest.getFormularioPagoDTO());
        logger.info("Método de pago: {}", pagoRequest.getMetodoDePago());
        logger.info("Productos: {}", pagoRequest.getProductos());

        if(metodoDePago == null || metodoDePago.isEmpty()){
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

        MercadoPagoConfig.setAccessToken("TEST-6775356251432010-052721-6aa60902aaef96aebd58e3b7112d3432-477764900");

        Usuario user = new Usuario(
                formularioPagoDTO.getEmail(),
                "",
                formularioPagoDTO.getNombre(),
                formularioPagoDTO.getApellido(),
                formularioPagoDTO.getTelefono(),
                formularioPagoDTO.getDni());
        Double totalCarrito = productoService.calcularValorTotalDeLosProductos();

        // Calcular el descuento si hay un código válido

        // Crea un objeto de preferencia
        PreferenceClient client = new PreferenceClient();

        // Crea un ítem en la preferencia
        List<PreferenceItemRequest> items = new ArrayList<>();
        for (int i = 0; i < pagoRequest.getProductos().size(); i++) {
            PreferenceItemRequest item =
                    PreferenceItemRequest.builder()
                            .title(pagoRequest.getProductos().get(i).getNombre() + " - " + pagoRequest.getProductos().get(i).getDescripcion())
                            .description(pagoRequest.getProductos().get(i).getDescripcion())
                            .quantity(pagoRequest.getProductos().get(i).getCantidad())
                            .currencyId("ARS")
                            .unitPrice(BigDecimal.valueOf(pagoRequest.getProductos().get(i).getPrecio()))
                            .build();

            items.add(item);
        }



        PreferencePayerRequest payer = PreferencePayerRequest.builder()
                .name(user.getNombre())
                .surname(user.getApellido())
                .email(user.getEmail()) // Correo de prueba de comprador
                .phone(PhoneRequest.builder()
                        .areaCode(user.getTelefono().substring(0, 2))
                        .number(user.getTelefono().substring(2, 8))
                        .build())
                .identification(IdentificationRequest.builder()
                        .type("DNI")
                        .number(user.getDni())
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

        Preference preference = client.create(preferenceRequest);

        guardarDatosCompra(cantidades, idsProductos, formularioPagoDTO.getEmail(), codigoTransaccion);


        response.sendRedirect(preference.getSandboxInitPoint());
        return null;
    }

    public void guardarDatosCompra(List<Integer> cantidades, List<Long> idsEntradas, String emailUsuario, String codigoTransaccion) {
        if (cantidades.size() != idsEntradas.size()) {
            throw new IllegalArgumentException("Las listas de cantidades e IDs de entradas deben tener el mismo tamaño");
        }

        DatosDeCompra datosCompraPendiente = new DatosDeCompra(codigoTransaccion, emailUsuario);
        for (int i = 0; i < idsEntradas.size(); i++) {
          //  ProductoCarritoDto producto = new ProductoCarritoDto("Mouse inalámbrico", 29.99);
           // datosCompraPendiente.agregarProducto(producto);
//            this.servicioProductoCompra.guardar(producto);
        }
//        this.servicioProductoCompra.guardar(datosCompraPendiente);
    }


    @GetMapping("/carritoDeCompras/compraFinalizada")
    public ModelAndView mostrarVistaCompraFinalizada(
            @RequestParam("codigoTransaccion") String codigoTransaccion,
             @RequestParam("status") String status){
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