package com.tallerwebi.presentacion.Kiosquero;

import com.tallerwebi.dominio.Pedidos.Pedido;
import com.tallerwebi.dominio.Pedidos.ServicioPedido;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class KiosqHomeControlador {

  private final ServicioPedido servicioPedido;

  @Autowired
  public KiosqHomeControlador(ServicioPedido servicioPedido) {
    this.servicioPedido = servicioPedido;
  }

  @RequestMapping(path = "/homeKiosquero", method = RequestMethod.GET)
  public ModelAndView irAlHomeKiosquero(
    HttpSession session,
    @RequestParam(value = "estado", required = false) String estadoPedido,
    @RequestParam(value = "busqueda", required = false) String busqueda
  ) {
    Usuario usuario = (Usuario) session.getAttribute("USUARIO");
    String rol = (String) session.getAttribute("ROL");

    // Si no está logueado o no es KIOSQUERO, afuera
    if (usuario == null || !"KIOSQUERO".equals(rol)) {
      return new ModelAndView("redirect:/login");
    }
    ModelMap modelo = new ModelMap();
    modelo.put("usuario", usuario);

    modelo.put("estados", com.tallerwebi.dominio.Pedidos.EstadoPedido.values());
    modelo.put("estadoActual", estadoPedido);

    this.cargarPedidosDeLosUsuariosCLientes(modelo, estadoPedido);
    this.cargarResultadoBusquedaPedido(modelo, busqueda);

    return new ModelAndView("homeKiosquero", modelo);
  }

  private void cargarPedidosDeLosUsuariosCLientes(ModelMap modelo, String estadoPedido) {
    try {
      List<Pedido> pedidosClientes;
      if (estadoPedido != null && !estadoPedido.isEmpty() && !"TODOS".equals(estadoPedido)) {
        pedidosClientes = this.servicioPedido.obtenerPedidosDeLosUsuariosFiltrado(estadoPedido);
      } else {
        pedidosClientes = this.servicioPedido.obtenerPedidosDeLosUsuarios();
      }
      modelo.put("pedidosClientes", pedidosClientes);
    } catch (PedidoNoEncontradoException e) {
      modelo.put("errorBusquedaPedido", e.getMessage());
    }
  }

  private void cargarResultadoBusquedaPedido(ModelMap modelo, String busqueda) {
    try {
      if (busqueda != null && !busqueda.trim().isEmpty()) {
        List<Pedido> pedidosBuscados =
          this.servicioPedido.obtenerResultadosBusquedaPorNombre(busqueda);
        modelo.put("pedidosBuscados", pedidosBuscados);
      }
    } catch (PedidoNoEncontradoException e) {
      modelo.put("errorBusquedaPedido", e.getMessage());
    }
  }
}
