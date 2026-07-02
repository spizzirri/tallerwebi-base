package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Carrito.Carrito;
import com.tallerwebi.dominio.Carrito.ItemCarrito;
import com.tallerwebi.dominio.Carrito.ServicioCarrito;
import com.tallerwebi.dominio.Hijos.Hijo;
import com.tallerwebi.dominio.Hijos.ServicioHijo;
import com.tallerwebi.dominio.Pedidos.ServicioPedido;
import com.tallerwebi.dominio.Productos.Producto;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.dominio.excepcion.FechaRetiroInvalidaException;
import com.tallerwebi.presentacion.DistribucionCarrito.DistribucionControlador;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.ModelAndView;

public class DistribucionControladorTest {

  private HttpSession sessionMock;
  private DistribucionControlador distriControlador;
  private ServicioCarrito serviCarritomock;
  private ServicioHijo serviHijoMock;
  private Usuario usuarioMock;
  private Carrito carritoMock;
  private Producto productoMock;
  private ItemCarrito itemMock;
  private Hijo hijoMock;
  private ServicioPedido serviPedidoMock;
  private LocalDate fechaRetiro;

  @BeforeEach
  public void init() {
    sessionMock = Mockito.mock(HttpSession.class);
    serviCarritomock = Mockito.mock(ServicioCarrito.class);
    serviHijoMock = Mockito.mock(ServicioHijo.class);
    serviPedidoMock = Mockito.mock(ServicioPedido.class);
    distriControlador =
      new DistribucionControlador(serviCarritomock, serviHijoMock, serviPedidoMock);
    usuarioMock = Mockito.mock(Usuario.class);
    carritoMock = Mockito.mock(Carrito.class);
    productoMock = Mockito.mock(Producto.class);
    itemMock = Mockito.mock(ItemCarrito.class);
    hijoMock = Mockito.mock(Hijo.class);
    fechaRetiro = LocalDate.of(2026, 7, 15);
  }

  @Test
  public void siNoHayUsuarioLogueadoDebeVolverALogin() {
    // preparacion
    when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

    // ejecucion
    ModelAndView mv = distriControlador.verDistribucion(sessionMock);

    // validacion
    assertThat(mv.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  @Test
  public void siHayUsuarioDebeMostrarLaVistaDeDistribucionDeCarrito() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);

    when(carritoMock.getItems()).thenReturn(new ArrayList<>());
    when(serviCarritomock.obtenerOCrearCarrito(any())).thenReturn(carritoMock);
    when(serviHijoMock.obtenerHijosPorUsuario(any())).thenReturn(new ArrayList<>());

    ModelAndView mv = distriControlador.verDistribucion(sessionMock);
    assertThat(mv.getViewName(), equalToIgnoringCase("carritoDistribucion"));
  }

  @Test
  public void siElUsuarioAgregoProductosEstosDebenEstarEnElCarrito() {
    usuarioMock.setId(1L);
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);

    when(productoMock.getNombre()).thenReturn("Mogul");
    when(itemMock.getProducto()).thenReturn(productoMock);

    when(carritoMock.getItems()).thenReturn(List.of(itemMock));
    when(serviCarritomock.obtenerOCrearCarrito(any())).thenReturn(carritoMock);

    when(serviHijoMock.obtenerHijosPorUsuario(any())).thenReturn(new ArrayList<>());

    ModelAndView mv = distriControlador.verDistribucion(sessionMock);

    List<Producto> productos = (List<Producto>) mv.getModel().get("productos");
    assertThat(productos.size(), equalTo(1));
    assertThat(productos.get(0).getNombre(), equalToIgnoringCase("Mogul"));
  }

  @Test
  public void losHijosDelUsuarioDebenAparecerEnLaTablaDeProductos() {
    usuarioMock.setId(1L);
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);

    when(carritoMock.getItems()).thenReturn(new ArrayList<>());
    when(serviCarritomock.obtenerOCrearCarrito(any())).thenReturn(carritoMock);
    when(hijoMock.getNombre()).thenReturn("Santi");
    when(serviHijoMock.obtenerHijosPorUsuario(any())).thenReturn(List.of(hijoMock));

    ModelAndView mv = distriControlador.verDistribucion(sessionMock);

    List<Hijo> hijos = (List<Hijo>) mv.getModel().get("hijos");
    assertThat(hijos.size(), equalTo(1));
    assertThat(hijos.get(0).getNombre(), equalToIgnoringCase("Santi"));
  }

  @Test
  public void confirmarPedidoConCantidadesValidasDebeReTornarOKyRedirigirAlCarrito() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);

    Map<String, String> params = new HashMap<>();
    params.put("hijoId1_prodId1", "2"); //lo de detras de la coma es la cantidad
    params.put("hijoId2_prodId1", "3");

    ModelAndView mv = distriControlador.confirmarPedido(params, fechaRetiro, sessionMock);

    assertThat(mv.getViewName(), equalToIgnoringCase("redirect:/carrito"));
  }

  @Test
  public void confirmarPedidoSinUsuarioDebeRedirigirALogin() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

    Map<String, String> params = new HashMap<>();

    ModelAndView mv = distriControlador.confirmarPedido(params, fechaRetiro, sessionMock);

    assertThat(mv.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  @Test
  public void alConfirmarPedidoSeDebeLlamarAlServicioPedido() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    Map<String, String> params = new HashMap<>();
    params.put("hijoId1_prodId1", "2");
    params.put("hijoId1_prodId2", "3");

    distriControlador.confirmarPedido(params, fechaRetiro, sessionMock);

    Mockito.verify(serviPedidoMock).crearPedido(any(), any(), any(), any());
  }

  @Test
  public void cuandoLaFechaRetiroEsInvalidaDebeDevolverVistaConError() {
    // Preparación
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(usuarioMock.getId()).thenReturn(1L);

    when(serviCarritomock.obtenerOCrearCarrito(1L)).thenReturn(carritoMock);
    when(carritoMock.getItems()).thenReturn(new ArrayList<>());

    when(serviHijoMock.obtenerHijosPorUsuario(1L)).thenReturn(new ArrayList<>());

    when(serviPedidoMock.obtenerPedidosPendientesDePago(1L)).thenReturn(new ArrayList<>());

    doThrow(new FechaRetiroInvalidaException("La fecha de retiro debe ser a partir de mañana."))
      .when(serviPedidoMock)
      .crearPedido(anyLong(), anyList(), any(LocalDate.class), eq(usuarioMock));

    Map<String, String> params = new HashMap<>();
    params.put("hijoId1_prodId1", "2");

    // Ejecución
    ModelAndView mv = distriControlador.confirmarPedido(params, fechaRetiro, sessionMock);

    // Verificación
    assertThat(mv.getViewName(), is("carritoDistribucion"));
    assertThat(mv.getModel().get("error"), is("La fecha de retiro debe ser a partir de mañana."));
  }
}
