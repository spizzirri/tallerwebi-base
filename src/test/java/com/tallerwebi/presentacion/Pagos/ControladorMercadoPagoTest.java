package com.tallerwebi.presentacion.Pagos;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Carrito.Carrito;
import com.tallerwebi.dominio.Carrito.ItemCarrito;
import com.tallerwebi.dominio.Carrito.ServicioCarrito;
import com.tallerwebi.dominio.Mail.ServicioEmail;
import com.tallerwebi.dominio.Pagos.ServicioMercadoPago;
import com.tallerwebi.dominio.Pedidos.Pedido;
import com.tallerwebi.dominio.Pedidos.ServicioPedido;
import com.tallerwebi.dominio.Productos.Producto;
import com.tallerwebi.dominio.Usuario.Usuario;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class ControladorMercadoPagoTest {

  private ServicioMercadoPago servicioMercadoPago;
  private ServicioCarrito servicioCarrito; // <-- NUEVO MOCK
  private ControladorMercadoPago controladorMercadoPago;
  private HttpSession sessionMock;
  private Usuario usuarioMock;
  private ServicioPedido servicioPedidoMock;
  private ServicioEmail servicioEmailMock;
  private RedirectAttributes flashMock;

  @BeforeEach
  public void init() {
    this.servicioMercadoPago = mock(ServicioMercadoPago.class);
    this.servicioCarrito = mock(ServicioCarrito.class); // <-- Inicializamos el mock del carrito
    this.servicioPedidoMock = mock(ServicioPedido.class);
    this.servicioEmailMock = mock(ServicioEmail.class);
    // Le pasamos ambos servicios al controlador unificado
    this.controladorMercadoPago =
      new ControladorMercadoPago(
        this.servicioMercadoPago,
        this.servicioCarrito,
        servicioPedidoMock,
        servicioEmailMock
      );
    this.sessionMock = mock(HttpSession.class);
    this.flashMock = mock(RedirectAttributes.class);

    // Preparamos un usuario mockeado con ID ficticio
    this.usuarioMock = mock(Usuario.class);
    when(this.usuarioMock.getId()).thenReturn(1L);
    // Hacemos que la sesión siempre devuelva este usuario para simular que está logueado
    when(this.sessionMock.getAttribute("USUARIO")).thenReturn(this.usuarioMock);
    when(usuarioMock.getNombre()).thenReturn("Test");
    when(usuarioMock.getEmail()).thenReturn("test@mail.com");
  }

  @Test
  public void siNoHayPedidosEnCarritoDebeRedirigirAlCarritoConMensajeDeError() {
    // 1. Given
    when(servicioPedidoMock.obtenerPedidosPendientesDePago(1L)).thenReturn(new ArrayList<>());

    // 2. When:
    ModelAndView modelAndView = this.controladorMercadoPago.pagar(this.sessionMock, this.flashMock);

    // 3. Then
    assertThat(modelAndView.getViewName(), equalTo("redirect:/carrito"));
    verify(flashMock).addFlashAttribute("errorDistribucion", "No hay pedidos para pagar");
  }

  @Test
  public void alPagarDebeMarcarLosPedidosComoPendientesYRedirigirAlCheckoutDeMercadoPago() {
    // 1. Given
    Pedido pedido = new Pedido();
    List<Pedido> pedidos = List.of(pedido);

    when(servicioPedidoMock.obtenerPedidosEnCarrito(1L)).thenReturn(pedidos);

    when(servicioMercadoPago.crearPreferenciaDePago(pedidos)).thenReturn("https://mp.com/redirect");
    // 2. When
    ModelAndView modelAndView = this.controladorMercadoPago.pagar(this.sessionMock, this.flashMock);

    // 3. Then
    assertThat(modelAndView.getViewName(), equalTo("redirect:https://mp.com/redirect"));
    verify(servicioPedidoMock).marcarPedidosEnCarritoComoPendientes(1L);
  }

  @Test
  public void siElUsuarioNoEstaLogueadoDebeRedirigirAlLogin() {
    // La sesión no tiene al usuario cargado
    when(this.sessionMock.getAttribute("USUARIO")).thenReturn(null);

    // Intenta pagar
    ModelAndView modelAndView = this.controladorMercadoPago.pagar(this.sessionMock, this.flashMock);

    // Redirige al login (Cubre el primer IF del controlador)
    assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
  }

  @Test
  public void siMercadoPagoFallaDebeMantenerElPedidoComoPendienteYRedirigirAMisPedidosConMensajeDeError() {
    // 1. Given
    Pedido pedido = new Pedido();
    List<Pedido> pedidos = List.of(pedido);

    when(servicioPedidoMock.obtenerPedidosEnCarrito(1L)).thenReturn(pedidos);

    when(servicioMercadoPago.crearPreferenciaDePago(pedidos)).thenReturn(null);

    // 3. When
    ModelAndView mav = controladorMercadoPago.pagar(sessionMock, this.flashMock);

    // 4. Then
    assertThat(mav.getViewName(), equalTo("redirect:/mis-pedidos"));
    verify(servicioPedidoMock).marcarPedidosEnCarritoComoPendientes(1L);
    verify(flashMock)
      .addFlashAttribute(
        eq("mensajeError"),
        contains("Tu pedido quedó guardado como pendiente de pago")
      );
  }

  @Test
  public void siElUsuarioNoEstaLogueadoDebeRedirigirAlLoginCuandoEntraAlPagoExitoso() {
    // La sesión no tiene al usuario cargado
    when(this.sessionMock.getAttribute("USUARIO")).thenReturn(null);

    // Intenta ingresar a pagar-exitoso sin estar logeado
    ModelAndView modelAndView =
      this.controladorMercadoPago.mostrarPagoExitoso(this.sessionMock, "1");
    // Redirige al login ()
    assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
  }

  @Test
  public void siNoHayExternalReferenceDebeRedirigirAHome() {
    ModelAndView mav = controladorMercadoPago.mostrarPagoExitoso(sessionMock, null);

    assertThat(mav.getViewName(), equalTo("redirect:/home"));
  }

  @Test
  public void siExternalReferenceNoResuelveNingunPedidoDebeRedirigirAHome() {
    when(servicioPedidoMock.buscarPorId(99L)).thenReturn(null);

    ModelAndView mav = controladorMercadoPago.mostrarPagoExitoso(sessionMock, "99");

    assertThat(mav.getViewName(), equalTo("redirect:/home"));
  }

  @Test
  public void conExternalReferenceValidoDebeMarcarSoloEsosPedidosComoPagadosYDevolverlosEnElModel() {
    Pedido pedido = mock(Pedido.class);
    when(pedido.getItems()).thenReturn(new ArrayList<>());
    when(servicioPedidoMock.buscarPorId(5L)).thenReturn(pedido);

    ModelAndView mav = controladorMercadoPago.mostrarPagoExitoso(sessionMock, "5");

    assertThat(mav.getViewName(), equalTo("pago-exitoso"));
    assertThat(mav.getModel().get("pedidos"), equalTo(List.of(pedido)));
    verify(servicioPedidoMock).actualizarEstadoPedido(5L, "PAGADO");
    // No debe tocar otros pedidos del usuario que no vinieron en la referencia
    verify(servicioPedidoMock, never()).marcarComoPagados(anyLong());
  }

  @Test
  public void conVariosIdsEnExternalReferenceDebeMarcarTodosComoPagados() {
    Pedido pedido1 = mock(Pedido.class);
    Pedido pedido2 = mock(Pedido.class);
    when(pedido1.getItems()).thenReturn(new ArrayList<>());
    when(pedido2.getItems()).thenReturn(new ArrayList<>());
    when(servicioPedidoMock.buscarPorId(5L)).thenReturn(pedido1);
    when(servicioPedidoMock.buscarPorId(7L)).thenReturn(pedido2);

    ModelAndView mav = controladorMercadoPago.mostrarPagoExitoso(sessionMock, "5,7");

    assertThat(mav.getViewName(), equalTo("pago-exitoso"));
    verify(servicioPedidoMock).actualizarEstadoPedido(5L, "PAGADO");
    verify(servicioPedidoMock).actualizarEstadoPedido(7L, "PAGADO");
  }
}
