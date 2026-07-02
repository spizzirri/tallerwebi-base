package com.tallerwebi.presentacion.Pagos;

import com.tallerwebi.dominio.Pagos.ServicioMercadoPago;
import com.tallerwebi.dominio.Pedidos.Pedido;
import com.tallerwebi.dominio.Pedidos.ServicioPedido;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControladorMisPedidos {

  private final ServicioPedido servicioPedido;
  private final ServicioMercadoPago servicioMercadoPago;

  @Autowired
  public ControladorMisPedidos(
    ServicioPedido servicioPedido,
    ServicioMercadoPago servicioMercadoPago
  ) {
    this.servicioPedido = servicioPedido;
    this.servicioMercadoPago = servicioMercadoPago;
  }

  @RequestMapping(path = "/mis-pedidos", method = RequestMethod.GET)
  public ModelAndView verMisPedidos(HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute("USUARIO");

    if (usuario == null) {
      return new ModelAndView("redirect:/login");
    }

    List<Pedido> pedidos = servicioPedido.obtenerTodosLosPedidos(usuario.getId());

    ModelMap model = new ModelMap();
    model.put("usuario", usuario);
    model.put("pedidos", pedidos);
    return new ModelAndView("mis-pedidos", model);
  }

  @PostMapping("/pedidos/cancelar")
  public ModelAndView cancelarPedido(
    @RequestParam("pedidoId") Long pedidoId,
    HttpSession session,
    RedirectAttributes flash
  ) {
    Usuario usuario = (Usuario) session.getAttribute("USUARIO");
    if (usuario == null) {
      return new ModelAndView("redirect:/login");
    }

    try {
      servicioPedido.cancelarPedido(pedidoId, usuario.getId());
      flash.addFlashAttribute("mensajeExito", "El pedido #" + pedidoId + " fue cancelado.");
    } catch (PedidoNoEncontradoException e) {
      flash.addFlashAttribute("mensajeError", "No se pudo cancelar el pedido: " + e.getMessage());
    }

    return new ModelAndView("redirect:/mis-pedidos");
  }

  @PostMapping("/pedidos/retomar-pago")
  public ModelAndView retomarPago(
    @RequestParam("pedidoId") Long pedidoId,
    HttpSession session,
    RedirectAttributes flash
  ) {
    Usuario usuario = (Usuario) session.getAttribute("USUARIO");
    if (usuario == null) {
      return new ModelAndView("redirect:/login");
    }

    Pedido pedido = servicioPedido.obtenerResultadosBusquedaPedidoPorId(pedidoId);

    if (pedido == null || !pedido.getUsuario().getId().equals(usuario.getId())) {
      flash.addFlashAttribute("mensajeError", "No se pudo encontrar el pedido para pagar.");
      return new ModelAndView("redirect:/mis-pedidos");
    }

    String urlPago = servicioMercadoPago.crearPreferenciaDePago(List.of(pedido));

    if (urlPago == null) {
      flash.addFlashAttribute(
        "mensajeError",
        "No se pudo conectar con Mercado Pago. Intentá nuevamente más tarde."
      );
      return new ModelAndView("redirect:/mis-pedidos");
    }

    return new ModelAndView("redirect:" + urlPago);
  }
}
