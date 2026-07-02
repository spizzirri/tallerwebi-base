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
import com.tallerwebi.dominio.excepcion.ProductoSinStockException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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
  private static final String HIJO_ID_CONSTANT = "hijoId";
  private static final String BORRADOR_SESSION = "borradorDistribucion";

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

    ModelMap model = prepararModeloDistribucion(usuario, session);
    Map<String, Integer> cantidadesPrevias = (Map<String, Integer>) model.get("cantidadesPrevias");

    // Si el borrador temporal está vacío, nos fijamos si estamos editando un pedido específico en esta sesión
    if (cantidadesPrevias == null || cantidadesPrevias.isEmpty()) {
      Long idPedidoActual = (Long) session.getAttribute("idPedidoActual");

      if (idPedidoActual != null) {
        Pedido pedido = servicioPedido.buscarPorId(idPedidoActual);
        if (pedido != null) {
          cantidadesPrevias = new HashMap<>();
          for (ItemPedido item : pedido.getItems()) {
            String clave = pedido.getHijo().getId() + "_" + item.getProducto().getId();
            cantidadesPrevias.put(clave, item.getCantidad());
          }
          model.put("cantidadesPrevias", cantidadesPrevias);
        }
      }
    }
    return new ModelAndView("carritoDistribucion", model);
  }

  @PostMapping("/distribucion/guardar-borrador")
  public ResponseEntity<Void> guardarBorradorTemporal(
    @RequestParam Map<String, String> params,
    HttpSession session
  ) {
    Map<String, Integer> borrador = new HashMap<>();

    for (Map.Entry<String, String> param : params.entrySet()) {
      if (param.getKey().startsWith(HIJO_ID_CONSTANT)) {
        String claveLimpia = param.getKey().replace(HIJO_ID_CONSTANT, "").replace("_prodId", "_");
        borrador.put(claveLimpia, Integer.parseInt(param.getValue()));
      }
    }

    session.setAttribute(BORRADOR_SESSION, borrador);
    return ResponseEntity.ok().build();
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

    Map<Long, List<ItemDistribucionDTO>> listaPorHijo = agruparItemsPorHijo(params);
    int totalProductosDistribuidos = contarProductosDistribuidos(listaPorHijo);

    if (totalProductosDistribuidos == 0) {
      ModelMap model = prepararModeloDistribucion(usuario, session);
      model.put(
        "error",
        "Debe asignar al menos una unidad de algún producto a sus hijos antes de confirmar."
      );
      return new ModelAndView("carritoDistribucion", model);
    }

    try {
      // Buscamos qué pedidos EN_CARRITO ya existen para este usuario, agrupados por hijo
      List<Pedido> pedidosExistentes = servicioPedido.obtenerPedidosEnCarrito(usuario.getId());
      Map<Long, Pedido> pedidoPorHijo = pedidosExistentes
        .stream()
        .collect(Collectors.toMap(p -> p.getHijo().getId(), p -> p));

      // Unimos: hijos que vinieron con productos en el form + hijos que ya tenían pedido EN_CARRITO
      // (esto cubre el caso de que un hijo se quede en 0 y haya que cancelar/vaciar su pedido)
      Set<Long> todosLosHijosAConsiderar = new HashSet<>();
      todosLosHijosAConsiderar.addAll(listaPorHijo.keySet());
      todosLosHijosAConsiderar.addAll(pedidoPorHijo.keySet());

      for (Long hijoId : todosLosHijosAConsiderar) {
        List<ItemDistribucionDTO> itemsDelHijo = listaPorHijo.getOrDefault(
          hijoId,
          new ArrayList<>()
        );
        Pedido pedidoExistente = pedidoPorHijo.get(hijoId);

        if (itemsDelHijo.isEmpty() && pedidoExistente == null) {
          // No hay nada nuevo y nunca hubo pedido para este hijo -> no hay nada que hacer
          continue;
        }
        Map<Long, List<ItemDistribucionDTO>> soloEsteHijo = new HashMap<>();
        soloEsteHijo.put(hijoId, itemsDelHijo);

        if (pedidoExistente != null) {
          // Ya había un pedido EN_CARRITO para este hijo -> lo actualizamos
          servicioPedido.actualizarPedidoExistente(
            pedidoExistente.getId(),
            soloEsteHijo,
            fechaRetiro,
            usuario
          );
        } else {
          // No había pedido EN_CARRITO para este hijo -> lo creamos
          servicioPedido.crearPedido(hijoId, itemsDelHijo, fechaRetiro, usuario);
        }
      }
      // por si el usuario va al carrito, agrega más cosas y vuelve.
      session.removeAttribute(BORRADOR_SESSION);
    } catch (FechaRetiroInvalidaException | ProductoSinStockException e) {
      ModelMap model = prepararModeloDistribucion(usuario, session);
      model.put("error", e.getMessage());
      return new ModelAndView("carritoDistribucion", model);
    }
    return new ModelAndView("redirect:/carrito");
  }

  @GetMapping("/pedidos/modificar/{id}")
  public String modificarPedido(@PathVariable Long id, HttpSession session) {
    Pedido pedido = servicioPedido.buscarPorId(id);

    // Armamos el mapa temporal simulando lo que el JS hubiera guardado
    Map<String, Integer> borradorRecuperado = new HashMap<>();
    for (ItemPedido item : pedido.getItems()) {
      // Estructura: "hijoId_productoId" -> cantidad
      String clave = pedido.getHijo().getId() + "_" + item.getProducto().getId();
      borradorRecuperado.put(clave, item.getCantidad());
    }

    // Lo subimos a la sesión para que la vista de distribución lo lea automáticamente
    session.setAttribute("borradorDistribucion", borradorRecuperado);

    return "redirect:/distribucion";
  }

  private ModelMap prepararModeloDistribucion(Usuario usuario, HttpSession session) {
    Carrito carrito = servicioCarrito.obtenerOCrearCarrito(usuario.getId());
    List<Hijo> hijos = servicioHijo.obtenerHijosPorUsuario(usuario.getId());

    List<Producto> productos = carrito
      .getItems()
      .stream()
      .map(ItemCarrito::getProducto)
      .collect(Collectors.toList());

    // 1. Base: lo que ya está confirmado en la base de datos (pedidos EN_CARRITO)
    Map<String, Integer> cantidadesPrevias = new HashMap<>();
    List<Pedido> pedidosEnCarrito = servicioPedido.obtenerPedidosEnCarrito(usuario.getId());

    for (Pedido pedido : pedidosEnCarrito) {
      for (ItemPedido item : pedido.getItems()) {
        String clave = pedido.getHijo().getId() + "_" + item.getProducto().getId();
        cantidadesPrevias.put(clave, item.getCantidad());
      }
    }
    // 2. Overlay: si hay ediciones sin confirmar en el borrador de sesión, pisan la base
    @SuppressWarnings("unchecked")
    Map<String, Integer> borrador = (Map<String, Integer>) session.getAttribute(BORRADOR_SESSION);
    if (borrador != null) {
      cantidadesPrevias.putAll(borrador);
    }

    ModelMap model = new ModelMap();
    model.put("productos", productos);
    model.put("hijos", hijos);
    model.put("usuario", usuario);
    model.put("cantidadesPrevias", cantidadesPrevias);

    return model;
  }

  private Map<Long, List<ItemDistribucionDTO>> agruparItemsPorHijo(Map<String, String> params) {
    Map<Long, List<ItemDistribucionDTO>> resultado = new HashMap<>();

    for (Map.Entry<String, String> param : params.entrySet()) {
      String nombreParametro = param.getKey();

      if (!nombreParametro.startsWith(HIJO_ID_CONSTANT)) {
        continue;
      }

      String[] partes = nombreParametro.split("_prodId");
      Long hijoId = Long.parseLong(partes[0].replace(HIJO_ID_CONSTANT, ""));
      Long productoId = Long.parseLong(partes[1]);
      Integer cantidad = Integer.parseInt(param.getValue());

      if (cantidad <= 0) {
        continue;
      }

      List<ItemDistribucionDTO> itemsDelHijo = resultado.computeIfAbsent(
        hijoId,
        k -> new ArrayList<>()
      );

      itemsDelHijo.add(new ItemDistribucionDTO(productoId, hijoId, cantidad));
    }
    return resultado;
  }

  private int contarProductosDistribuidos(Map<Long, List<ItemDistribucionDTO>> listaPorHijo) {
    int total = 0;
    for (List<ItemDistribucionDTO> items : listaPorHijo.values()) {
      for (ItemDistribucionDTO item : items) {
        total += item.getCantidad();
      }
    }
    return total;
  }
}
