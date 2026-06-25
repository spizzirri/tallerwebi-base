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

public class ControladorMercadoPagoTest {

  private ServicioMercadoPago servicioMercadoPago;
  private ServicioCarrito servicioCarrito; // <-- NUEVO MOCK
  private ControladorMercadoPago controladorMercadoPago;
  private HttpSession sessionMock;
  private Usuario usuarioMock;
  private ServicioPedido servicioPedidoMock;
  private ServicioEmail servicioEmailMock;

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

    // Preparamos un usuario mockeado con ID ficticio
    this.usuarioMock = mock(Usuario.class);
    when(this.usuarioMock.getId()).thenReturn(1L);
    // Hacemos que la sesión siempre devuelva este usuario para simular que está logueado
    when(this.sessionMock.getAttribute("USUARIO")).thenReturn(this.usuarioMock);
    when(usuarioMock.getNombre()).thenReturn("Test");
    when(usuarioMock.getEmail()).thenReturn("test@mail.com");
  }

  @Test
  public void siNoHayPedidosPendientesDebeRedirigirAlCarritoConMensajeDeError() {
    // 1. Given
    when(servicioPedidoMock.obtenerPedidosPendientesDePago(1L)).thenReturn(new ArrayList<>());

    // 2. When:
    ModelAndView modelAndView = this.controladorMercadoPago.pagar(this.sessionMock);

    // 3. Then
    assertThat(modelAndView.getViewName(), equalTo("redirect:/carrito"));
    assertThat(modelAndView.getModel().get("error"), equalTo("No hay pedidos para pagar"));
  }

  @Test
  public void alPagarDebeBuscarElPedidoEnLaBaseDeDatosYRedirigirAlCheckoutDeMercadoPago() {
    // 1. Given
    Pedido pedido = new Pedido();
    List<Pedido> pedidos = List.of(pedido);

    when(servicioPedidoMock.obtenerPedidosPendientesDePago(1L)).thenReturn(pedidos);

    when(servicioMercadoPago.crearPreferenciaDePago(pedidos)).thenReturn("https://mp.com/redirect");
    // 2. When
    ModelAndView modelAndView = this.controladorMercadoPago.pagar(this.sessionMock);

    // 3. Then
    assertThat(modelAndView.getViewName(), equalTo("redirect:https://mp.com/redirect"));
  }

  @Test
  public void siElUsuarioNoEstaLogueadoDebeRedirigirAlLogin() {
    // La sesión no tiene al usuario cargado
    when(this.sessionMock.getAttribute("USUARIO")).thenReturn(null);

    // Intenta pagar
    ModelAndView modelAndView = this.controladorMercadoPago.pagar(this.sessionMock);

    // Redirige al login (Cubre el primer IF del controlador)
    assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
  }

  @Test
  public void siMercadoPagoFallaDebeRedirigirAlCarritoConMensajeDeError() {
    // 1. Given
    Pedido pedido = new Pedido();
    List<Pedido> pedidos = List.of(pedido);

    when(servicioPedidoMock.obtenerPedidosPendientesDePago(1L)).thenReturn(pedidos);

    when(servicioMercadoPago.crearPreferenciaDePago(pedidos)).thenReturn(null);

    // 3. When
    ModelAndView mav = controladorMercadoPago.pagar(sessionMock);

    // 4. Then
    assertThat(mav.getViewName(), equalTo("redirect:/carrito"));
    assertThat(
      mav.getModel().get("error").toString(),
      equalTo("No se pudo conectar con Mercado Pago. Intente más tarde.")
    );
  }

  @Test
  public void siElUsuarioNoEstaLogueadoDebeRedirigirAlLoginCuandoEntraAlPagoExitoso() {
    // La sesión no tiene al usuario cargado
    when(this.sessionMock.getAttribute("USUARIO")).thenReturn(null);

    // Intenta ingresar a pagar-exitoso sin estar logeado
    ModelAndView modelAndView = this.controladorMercadoPago.mostrarPagoExitoso(this.sessionMock);
    // Redirige al login ()
    assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
  }

  @Test
  public void ElUsuarioEstaLogueadoCuandoEntraAlPagoExitosoSinItemsEnElCarrito() {
    //    // La sesión ya tiene al usuario cargado
    //    Carrito carritoVacio = new Carrito();
    //    carritoVacio.setItems(new ArrayList<>());
    //    //tiene carrito vacio
    //    when(this.servicioCarrito.obtenerOCrearCarrito(1L)).thenReturn(carritoVacio);
    //    // Intenta ingresar a pagar-exitoso estando logeado pero sin carrito con items
    //    ModelAndView mav = this.controladorMercadoPago.mostrarPagoExitoso(this.sessionMock);
    //    // Redirige al carrito con error ()
    //    assertThat(mav.getViewName(), equalTo("redirect:/carrito"));
    //    assertThat(
    //      mav.getModel().get("error").toString(),
    //      equalTo("Debés agregar items y realizar una compra primero.")
    //    );
    when(servicioPedidoMock.obtenerPedidosPendientesDePago(1L)).thenReturn(new ArrayList<>());

    ModelAndView mav = controladorMercadoPago.mostrarPagoExitoso(sessionMock);

    assertThat(mav.getViewName(), equalTo("redirect:/home"));
  }

  @Test
  public void ElUsuarioEstaLogueadoCuandoEntraAlPagoExitosoConItemsEnElCarritoDebeDevolverEstosEnElModel() {
    //    Carrito carritoConProducto = new Carrito();
    //    List<ItemCarrito> items = new ArrayList<>();
    //    Producto producto = new Producto();
    //    items.add(new ItemCarrito(producto, 1));
    //    carritoConProducto.setItems(items);
    //
    //    when(this.servicioCarrito.obtenerOCrearCarrito(1L)).thenReturn(carritoConProducto);
    //
    //    // 2. When
    //    ModelAndView modelAndView = this.controladorMercadoPago.mostrarPagoExitoso(this.sessionMock);
    //
    //    // 3. Then
    //    assertThat(modelAndView.getViewName(), equalTo("pago-exitoso"));
    //    assertThat(modelAndView.getModel().get("itemsComprados"), equalTo(items));

    Pedido pedido = mock(Pedido.class);
    List<Pedido> pedidos = List.of(pedido);
    when(servicioPedidoMock.obtenerPedidosPendientesDePago(1L)).thenReturn(pedidos);

    ModelAndView mav = controladorMercadoPago.mostrarPagoExitoso(sessionMock);

    assertThat(mav.getViewName(), equalTo("pago-exitoso"));
    assertThat(mav.getModel().get("pedidos"), equalTo(pedidos));
  }
}
