package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Carrito.Carrito;
import com.tallerwebi.dominio.Carrito.ItemCarrito;
import com.tallerwebi.dominio.Carrito.ServicioCarrito;
import com.tallerwebi.dominio.Hijos.Hijo;
import com.tallerwebi.dominio.Hijos.ServicioHijo;
import com.tallerwebi.dominio.Pedidos.Pedido;
import com.tallerwebi.dominio.Pedidos.ServicioPedido;
import com.tallerwebi.dominio.Productos.Producto;
import com.tallerwebi.dominio.Usuario.Usuario;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class CarritoControladorTest {

  private HttpSession sessionMock;
  private CarritoControlador carritoControlador;
  private ServicioCarrito serviCarritomock;
  private ServicioHijo serviHijoMock;
  private Usuario usuarioMock;
  private Carrito carritoMock;
  private RedirectAttributes redirectAttributesMock;

  private Producto productoMock;
  private ItemCarrito itemMock;
  private Hijo hijoMock;
  private ServicioPedido serviPedidoMock;

  @BeforeEach
  public void init() {
    sessionMock = mock(HttpSession.class);
    serviCarritomock = mock(ServicioCarrito.class);
    serviPedidoMock = mock(ServicioPedido.class);
    usuarioMock = mock(Usuario.class);
    carritoMock = mock(Carrito.class);
    productoMock = mock(Producto.class);
    itemMock = mock(ItemCarrito.class);
    hijoMock = mock(Hijo.class);
    carritoControlador = new CarritoControlador(serviCarritomock, serviPedidoMock);
    redirectAttributesMock = mock(RedirectAttributes.class);
  }

  @Test
  public void siNoHayUsuarioLogueadoDebeRedirigirALogin() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

    ModelAndView mv = carritoControlador.verCarrito(sessionMock, redirectAttributesMock);

    assertThat(mv.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  @Test
  public void siHayUsuarioDebeMostrarLaVistaDePedidos() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(serviPedidoMock.obtenerPedidosPendientesDePago(any()))
      .thenReturn(List.of(mock(Pedido.class)));

    ModelAndView mv = carritoControlador.verCarrito(sessionMock, redirectAttributesMock);

    assertThat(mv.getViewName(), equalToIgnoringCase("carrito"));
  }

  @Test
  public void siHayUsuarioLosPedidosDebenEstarEnElModelo() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(serviCarritomock.obtenerOCrearCarrito(any())).thenReturn(carritoMock);
    when(carritoMock.getItems()).thenReturn(new ArrayList<>());
    when(serviPedidoMock.obtenerPedidosPendientesDePago(any()))
      .thenReturn(List.of(mock(Pedido.class)));

    ModelAndView mv = carritoControlador.verCarrito(sessionMock, redirectAttributesMock);

    List<Pedido> pedidos = (List<Pedido>) mv.getModel().get("pedidos");
    assertThat(pedidos.size(), equalTo(1));
  }
}
