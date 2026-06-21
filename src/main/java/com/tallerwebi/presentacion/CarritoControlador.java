package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Carrito.ServicioCarrito;
import com.tallerwebi.dominio.Pedidos.ItemPedido;
import com.tallerwebi.dominio.Pedidos.Pedido;
import com.tallerwebi.dominio.Pedidos.ServicioPedido;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.dominio.excepcion.ProductoNoEncontradoException;
import com.tallerwebi.dominio.excepcion.ProductoSinStockException;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CarritoControlador {

  private final ServicioCarrito servicioCarrito;
  private final ServicioPedido servicioPedido;
  private static final String USUARIO = "USUARIO";
  private static final String CARRITO = "carrito";
  private static final String PRODUCTO_ID = "productoId";

  @Autowired
  public CarritoControlador(ServicioCarrito servicioCarrito, ServicioPedido servicioPedido) {
    this.servicioCarrito = servicioCarrito;
    this.servicioPedido = servicioPedido;
  }

  @RequestMapping(path = "/carrito/agregar", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<String> agregarProducto(
    @RequestParam(PRODUCTO_ID) Long productoId,
    HttpSession session
  ) {
    Usuario usuario = (Usuario) session.getAttribute(USUARIO);

    try {
      servicioCarrito.agregarProducto(productoId, usuario.getId());
      return ResponseEntity.ok("ok");
    } catch (ProductoNoEncontradoException e) {
      return ResponseEntity.status(400).body(e.getMessage());
    } catch (ProductoSinStockException e) {
      return ResponseEntity.status(400).body(e.getMessage());
    }
  }

  @RequestMapping(path = "/carrito", method = RequestMethod.GET)
  public ModelAndView verCarrito(HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute(USUARIO);
    if (usuario == null) {
      return new ModelAndView("redirect:/login");
    }
    List<Pedido> pedidos = servicioPedido.obtenerPedidosPendientesDePago(usuario.getId());

    // Si no hay pedidos, no dejamos entrar al carrito
    if (pedidos == null || pedidos.isEmpty()) {
      return new ModelAndView("redirect:/distribucion");
    }
    Double total = pedidos
      .stream()
      .flatMap(p -> p.getItems().stream())
      .mapToDouble(ItemPedido::getSubtotal)
      .sum();

    ModelMap model = new ModelMap();
    model.put("total", total);
    model.put("pedidos", pedidos);

    return new ModelAndView(CARRITO, model);
  }

  @RequestMapping(path = "/carrito/eliminar", method = RequestMethod.POST)
  public String eliminarProducto(@RequestParam(PRODUCTO_ID) Long productoId, HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute(USUARIO);

    servicioCarrito.eliminarProducto(productoId, usuario.getId());

    return "redirect:/carrito";
  }

  @RequestMapping(path = "/carrito/aumentar", method = RequestMethod.POST)
  public String aumentarCantidadDeProducto(
    @RequestParam(PRODUCTO_ID) Long productoId,
    HttpSession session
  ) {
    Usuario usuario = (Usuario) session.getAttribute(USUARIO);

    servicioCarrito.aumentarCantidad(productoId, usuario.getId());

    return "redirect:/carrito";
  }

  @RequestMapping(path = "/carrito/disminuir", method = RequestMethod.POST)
  public String restarCantidadDeProducto(
    @RequestParam(PRODUCTO_ID) Long productoId,
    HttpSession session
  ) {
    Usuario usuario = (Usuario) session.getAttribute(USUARIO);

    servicioCarrito.disminuirCantidad(productoId, usuario.getId());

    return "redirect:/carrito";
  }
}
