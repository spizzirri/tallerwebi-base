package com.tallerwebi.presentacion.DistribucionCarrito;

import com.tallerwebi.dominio.Carrito.Carrito;
import com.tallerwebi.dominio.Carrito.ItemCarrito;
import com.tallerwebi.dominio.Carrito.ServicioCarrito;
import com.tallerwebi.dominio.Hijos.Hijo;
import com.tallerwebi.dominio.Hijos.ServicioHijo;
import com.tallerwebi.dominio.Pedidos.ItemPedido;
import com.tallerwebi.dominio.Pedidos.Pedido;
import com.tallerwebi.dominio.Pedidos.ServicioPedido;
import com.tallerwebi.dominio.Productos.Producto;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.dominio.excepcion.FechaRetiroInvalidaException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class DistribucionControlador {

  private final ServicioCarrito servicioCarrito;
  private final ServicioHijo servicioHijo;
  private final ServicioPedido servicioPedido;
  private static final String USUARIO_SESSION = "USUARIO";

  @Autowired
  public DistribucionControlador(
    ServicioCarrito servicioCarrito,
    ServicioHijo servicioHijo,
    ServicioPedido servicioPedido
  ) {
    this.servicioCarrito = servicioCarrito;
    this.servicioHijo = servicioHijo;
    this.servicioPedido = servicioPedido;
  }

  @RequestMapping(path = "/distribucion", method = RequestMethod.GET)
  public ModelAndView verDistribucion(HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute(USUARIO_SESSION);

    if (usuario == null) {
      return new ModelAndView("redirect:/login");
    }

    Carrito carrito = servicioCarrito.obtenerOCrearCarrito(usuario.getId());
    List<Hijo> hijos = servicioHijo.obtenerHijosPorUsuario(usuario.getId());

    List<Producto> productos = carrito
      .getItems()
      .stream()
      .map(ItemCarrito::getProducto)
      .collect(Collectors.toList());

    List<Pedido> pedidosPrevios = servicioPedido.obtenerPedidosPendientesDePago(usuario.getId());

    // Armar un mapa hijoId_productoId -> cantidad para el HTML
    Map<String, Integer> cantidadesPrevias = new HashMap<>();
    for (Pedido p : pedidosPrevios) {
      for (ItemPedido item : p.getItems()) {
        String key = p.getHijo().getId() + "_" + item.getProducto().getId();
        cantidadesPrevias.put(key, item.getCantidad());
      }
    }

    ModelMap model = new ModelMap();
    model.put("productos", productos);
    model.put("hijos", hijos);
    model.put("usuario", usuario);
    model.put("cantidadesPrevias", cantidadesPrevias); // ← NUEVO

    return new ModelAndView("carritoDistribucion", model);
  }

  @PostMapping("/distribucion/confirmar")
  public ModelAndView confirmarPedido(
    @RequestParam Map<String, String> params,
    @RequestParam("fechaRetiro") @DateTimeFormat(
      iso = DateTimeFormat.ISO.DATE
    ) LocalDate fechaRetiro,
    HttpSession session
  ) {
    Usuario usuario = (Usuario) session.getAttribute(USUARIO_SESSION);

    if (usuario == null) {
      return new ModelAndView("redirect:/login");
    }

    Map<Long, List<ItemDistribucionDTO>> listaPorHijo = new HashMap<>();
    servicioPedido.limpiarPedidosPendientes(usuario.getId());

    for (Map.Entry<String, String> param : params.entrySet()) {
      String nombreParametro = param.getKey();

      if (!nombreParametro.startsWith("hijoId")) {
        continue;
      }

      String[] partes = nombreParametro.split("_prodId");

      Long hijoId = Long.parseLong(partes[0].replace("hijoId", ""));

      Long productoId = Long.parseLong(partes[1]);

      Integer cantidad = Integer.parseInt(param.getValue());

      if (cantidad <= 0) {
        continue;
      }

      List<ItemDistribucionDTO> itemsDelHijo = listaPorHijo.computeIfAbsent(
        hijoId,
        k -> new ArrayList<>()
      );

      itemsDelHijo.add(new ItemDistribucionDTO(productoId, hijoId, cantidad));
    }

    try {
      for (Map.Entry<Long, List<ItemDistribucionDTO>> entry : listaPorHijo.entrySet()) {
        if (!entry.getValue().isEmpty()) {
          servicioPedido.crearPedido(entry.getKey(), entry.getValue(), fechaRetiro, usuario);
        }
      }
    } catch (FechaRetiroInvalidaException e) {
      return devolverVistaConError(usuario, e.getMessage());
    }

    return new ModelAndView("redirect:/carrito");
  }

  // Métodos auxiliares
  private ModelAndView devolverVistaConError(Usuario usuario, String mensaje) {
    Carrito carrito = servicioCarrito.obtenerOCrearCarrito(usuario.getId());
    List<Hijo> hijos = servicioHijo.obtenerHijosPorUsuario(usuario.getId());

    List<Producto> productos = carrito
      .getItems()
      .stream()
      .map(ItemCarrito::getProducto)
      .collect(Collectors.toList());

    List<Pedido> pedidosPrevios = servicioPedido.obtenerPedidosPendientesDePago(usuario.getId());

    Map<String, Integer> cantidadesPrevias = new HashMap<>();

    for (Pedido pedido : pedidosPrevios) {
      for (ItemPedido item : pedido.getItems()) {
        String key = pedido.getHijo().getId() + "_" + item.getProducto().getId();
        cantidadesPrevias.put(key, item.getCantidad());
      }
    }

    ModelMap model = new ModelMap();
    model.put("productos", productos);
    model.put("hijos", hijos);
    model.put("usuario", usuario);
    model.put("cantidadesPrevias", cantidadesPrevias);
    model.put("error", mensaje);

    return new ModelAndView("carritoDistribucion", model);
  }
}
