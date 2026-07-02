package com.tallerwebi.presentacion.Pagos;

import com.tallerwebi.dominio.Carrito.ServicioCarrito;
import com.tallerwebi.dominio.Mail.ServicioEmail;
import com.tallerwebi.dominio.Pagos.ServicioMercadoPago;
import com.tallerwebi.dominio.Pedidos.Pedido;
import com.tallerwebi.dominio.Pedidos.ServicioPedido;
import com.tallerwebi.dominio.Usuario.Usuario;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControladorMercadoPago {

  private static final Logger logger = LoggerFactory.getLogger(ControladorMercadoPago.class);

  private final ServicioMercadoPago servicioMercadoPago;
  private final ServicioCarrito servicioCarrito;
  private final ServicioPedido servicioPedido;

  @Autowired
  private ServicioEmail servicioEmail;

  @Autowired
  public ControladorMercadoPago(
    ServicioMercadoPago servicioMercadoPago,
    ServicioCarrito servicioCarrito,
    ServicioPedido servicioPedido,
    ServicioEmail servicioEmail
  ) {
    this.servicioMercadoPago = servicioMercadoPago;
    this.servicioCarrito = servicioCarrito;
    this.servicioPedido = servicioPedido;
    this.servicioEmail = servicioEmail;
  }

  @RequestMapping(path = "/pagar", method = RequestMethod.GET)
  public ModelAndView pagar(HttpSession session, RedirectAttributes flash) {
    Usuario usuario = (Usuario) session.getAttribute("USUARIO");

    if (usuario == null) {
      return new ModelAndView("redirect:/login");
    }

    List<Pedido> pedidosAPagar = servicioPedido.obtenerPedidosEnCarrito(usuario.getId());

    if (pedidosAPagar.isEmpty()) {
      flash.addFlashAttribute("errorDistribucion", "No hay pedidos para pagar");
      return new ModelAndView("redirect:/carrito");
    }

    // 2. IMPORTANTE: pasamos a PAGO_PENDIENTE ANTES de llamar a MercadoPago.
    // Así, si la API de MercadoPago falla, el pedido ya quedó en un estado
    // consistente (PAGO_PENDIENTE) en vez de perderse o quedar trabado en EN_CARRITO.
    servicioPedido.marcarPedidosEnCarritoComoPendientes(usuario.getId());

    // 3. Recién ahora intentamos generar el link de pago
    String urlPago = servicioMercadoPago.crearPreferenciaDePago(pedidosAPagar);

    if (urlPago == null) {
      // El pedido YA quedó en PAGO_PENDIENTE (paso 2), no lo revertimos.
      // El usuario podrá verlo en "Mis Pedidos" y reintentar el pago más tarde.
      flash.addFlashAttribute(
        "mensajeError",
        "No se pudo conectar con Mercado Pago. Tu pedido quedó guardado como pendiente de pago, podés reintentar el pago más tarde."
      );
      return new ModelAndView("redirect:/mis-pedidos");
    }

    return new ModelAndView("redirect:" + urlPago);
  }

  @RequestMapping(path = "/pago-exitoso", method = RequestMethod.GET)
  public ModelAndView mostrarPagoExitoso(
    HttpSession session,
    @RequestParam(value = "external_reference", required = false) String externalReference
  ) {
    Usuario usuario = (Usuario) session.getAttribute("USUARIO");

    if (usuario == null) {
      return new ModelAndView("redirect:/login");
    }

    // Como /pagar ya dejó estos pedidos en PAGO_PENDIENTE antes de mandar a MercadoPago,
    // acá simplemente los tomamos y los pasamos a PAGADO.
    List<Pedido> pedidosPagados = new ArrayList<>();

    if (externalReference != null && !externalReference.isEmpty()) {
      // Camino correcto: sabemos EXACTAMENTE qué pedidos se pagaron en este intento
      for (String idStr : externalReference.split(",")) {
        try {
          Long id = Long.parseLong(idStr.trim());
          Pedido pedido = servicioPedido.buscarPorId(id);
          if (pedido != null) {
            pedidosPagados.add(pedido);
            servicioPedido.actualizarEstadoPedido(id, "PAGADO");
          }
        } catch (NumberFormatException e) {
          logger.warn("No se pudo parsear un ID de pedido en external_reference: '{}'", idStr, e);
        }
      }
    }

    if (pedidosPagados.isEmpty()) {
      return new ModelAndView("redirect:/home");
    }
    Double total = pedidosPagados
      .stream()
      .flatMap(p -> p.getItems().stream())
      .mapToDouble(i -> i.getCantidad() * i.getProducto().getPrecio())
      .sum();

    String mensaje =
      "Hola " +
      usuario.getNombre() +
      "\n\n" +
      "Recibimos correctamente tu pago.\n\n" +
      "Total abonado: $" +
      total +
      "\n\n" +
      "Gracias por utilizar Kionet.";

    servicioEmail.enviarEmail(usuario.getEmail(), "Pago recibido - Kionet", mensaje);

    servicioCarrito.vaciarCarrito(usuario.getId());

    ModelMap model = new ModelMap();
    model.put("pedidos", pedidosPagados);
    return new ModelAndView("pago-exitoso", model);
  }
}
