package com.tallerwebi.dominio.Pedido;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Hijos.Hijo;
import com.tallerwebi.dominio.Hijos.RepositorioHijo;
import com.tallerwebi.dominio.Pedidos.Pedido;
import com.tallerwebi.dominio.Pedidos.RepositorioPedido;
import com.tallerwebi.dominio.Pedidos.ServicioPedido;
import com.tallerwebi.dominio.Pedidos.ServicioPedidoImpl;
import com.tallerwebi.dominio.Productos.Producto;
import com.tallerwebi.dominio.Productos.RepositorioProducto;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.dominio.excepcion.FechaRetiroInvalidaException;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import com.tallerwebi.presentacion.DistribucionCarrito.ItemDistribucionDTO;
import java.time.LocalDate;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ServicioPedidoTest {

  private HttpSession sessionMock;
  private Usuario usuarioMock;
  private ServicioPedido servicioPedido;
  private RepositorioPedido repositorioPedidoMock;
  private Hijo hijoMock;
  private RepositorioHijo repositorioHijoMock;
  private RepositorioProducto repositorioProductoMock;
  private Producto productoMock;
  private LocalDate fechaRetiro;

  @BeforeEach
  public void init() {
    sessionMock = mock(HttpSession.class);
    usuarioMock = mock(Usuario.class);
    repositorioPedidoMock = mock(RepositorioPedido.class);
    repositorioHijoMock = mock(RepositorioHijo.class);
    repositorioProductoMock = mock(RepositorioProducto.class);
    servicioPedido =
      new ServicioPedidoImpl(repositorioPedidoMock, repositorioHijoMock, repositorioProductoMock);
    hijoMock = mock(Hijo.class);
    productoMock = mock(Producto.class);
    fechaRetiro = LocalDate.now().plusDays(1);
  }

  @Test
  public void dadoUnHijoConItemsAlConfirmarPedidoDebeGuardarloEnElRepositorio() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);

    when(productoMock.getPrecio()).thenReturn(100.0);
    when(repositorioProductoMock.buscarProductoPorId(1L)).thenReturn(productoMock);

    List<ItemDistribucionDTO> items = List.of(new ItemDistribucionDTO(1L, 1L, 2));
    servicioPedido.crearPedido(1L, items, fechaRetiro, usuarioMock);
    verify(repositorioPedidoMock).guardar(any(Pedido.class));
  }

  @Test
  public void dadoUnHijoAlCrearPedidoElPedidoDebeTenerEseHijo() {
    when(hijoMock.getId()).thenReturn(1L);
    when(productoMock.getPrecio()).thenReturn(100.0);

    when(repositorioHijoMock.buscarPorId(1L)).thenReturn(hijoMock);
    when(repositorioProductoMock.buscarProductoPorId(1L)).thenReturn(productoMock);

    List<ItemDistribucionDTO> items = List.of(new ItemDistribucionDTO(1L, 1L, 2));

    servicioPedido.crearPedido(1L, items, fechaRetiro, usuarioMock);

    verify(repositorioPedidoMock).guardar(argThat(pedido -> pedido.getHijo() != null));
  }

  @Test
  public void dadoUnItemConCantidad2ElPedidoDebeCrearseConEsaCantidad() {
    when(productoMock.getPrecio()).thenReturn(100.0);
    when(repositorioHijoMock.buscarPorId(1L)).thenReturn(hijoMock);
    when(repositorioProductoMock.buscarProductoPorId(1L)).thenReturn(productoMock);

    List<ItemDistribucionDTO> items = List.of(new ItemDistribucionDTO(1L, 1L, 2));

    servicioPedido.crearPedido(1L, items, fechaRetiro, usuarioMock);

    verify(repositorioPedidoMock)
      .guardar(argThat(pedido -> pedido.getItems().get(0).getCantidad() == 2));
  }

  @Test
  public void dadoUnUsuarioConPedidosPendientesDebeRetornarlos() {
    Pedido pedidoMock = mock(Pedido.class);
    when(repositorioPedidoMock.obtenerPedidosPorUsuario(1L)).thenReturn(List.of(pedidoMock));

    List<Pedido> pedidos = servicioPedido.obtenerPedidosPendientesDePago(1L);

    assertThat(pedidos.size(), equalTo(1));
  }

  @Test
  public void cuandoLaFechaRetiroEsNulaDebeLanzarFechaRetiroInvalidaException() {
    List<ItemDistribucionDTO> items = List.of(new ItemDistribucionDTO(1L, 1L, 2));

    assertThrows(
      FechaRetiroInvalidaException.class,
      () -> servicioPedido.crearPedido(1L, items, null, usuarioMock)
    );
  }

  @Test
  public void cuandoLaFechaRetiroEsHoyDebeLanzarFechaRetiroInvalidaException() {
    List<ItemDistribucionDTO> items = List.of(new ItemDistribucionDTO(1L, 1L, 2));

    assertThrows(
      FechaRetiroInvalidaException.class,
      () -> servicioPedido.crearPedido(1L, items, LocalDate.now(), usuarioMock)
    );
  }

  @Test
  public void cuandoLaFechaRetiroEsAnteriorAHoyDebeLanzarFechaRetiroInvalidaException() {
    List<ItemDistribucionDTO> items = List.of(new ItemDistribucionDTO(1L, 1L, 2));

    assertThrows(
      FechaRetiroInvalidaException.class,
      () -> servicioPedido.crearPedido(1L, items, LocalDate.now().minusDays(1), usuarioMock)
    );
  }

  @Test
  public void dadoUnUsuarioKiosqueroDebeRetornarVistaConPedidosDeLosClientes() {
    Pedido pedidoMock = mock(Pedido.class);
    when(repositorioPedidoMock.obtenerTodosLosPedidosDeTodosLosClientes())
      .thenReturn(List.of(pedidoMock));

    List<Pedido> pedidosClientes = servicioPedido.obtenerPedidosDeLosUsuarios();
    assertThat(pedidosClientes.size(), equalTo(1));
  }

  @Test
  public void dadoUnUsuarioKiosqueroDebeRetornarVistaConPedidosFiltradoPorEstado() {
    Pedido pedidoMock = mock(Pedido.class);
    when(repositorioPedidoMock.obtenerTodosLosPedidosDeTodosLosClientesFiltrado("PAGADO"))
      .thenReturn(List.of(pedidoMock));

    List<Pedido> pedidosClientes = servicioPedido.obtenerPedidosDeLosUsuariosFiltrado("PAGADO");

    assertThat(pedidosClientes.size(), equalTo(1));
  }

  @Test
  public void dadoUnNombreDeAlumnoDebeInvocarAlRepositorioYRetornarLosPedidos() {
    Pedido pedidoMock = mock(Pedido.class);
    when(repositorioPedidoMock.buscarPedidosPorNombreDelAlumno("Santi"))
      .thenReturn(List.of(pedidoMock));

    List<Pedido> resultados = servicioPedido.obtenerResultadosBusquedaPorNombre("Santi");

    assertThat(resultados, hasSize(1));
    verify(repositorioPedidoMock, times(1)).buscarPedidosPorNombreDelAlumno("Santi");
  }

  @Test
  public void dadoUnIdDePedidoDebeInvocarAlRepositorioYRetornarElPedido() {
    Pedido pedidoMock = mock(Pedido.class);
    when(repositorioPedidoMock.buscarPedidoPorId(1L)).thenReturn(pedidoMock);

    Pedido resultado = servicioPedido.obtenerResultadosBusquedaPedidoPorId(1L);

    assertThat(resultado, notNullValue());
    verify(repositorioPedidoMock, times(1)).buscarPedidoPorId(1L);
  }

  @Test
  public void alActualizarEstadoSiElPedidoExisteDebeModificarlo() {
    Pedido pedidoMock = mock(Pedido.class);
    // Simula que el pedido SÍ existe en la base de datos
    when(repositorioPedidoMock.buscarPedidoPorId(1L)).thenReturn(pedidoMock);

    servicioPedido.actualizarEstadoPedido(1L, "ENTREGADO");

    verify(repositorioPedidoMock, times(1)).cambiarEstadoPedido(1L, "ENTREGADO");
  }

  @Test
  public void alActualizarEstadoSiElPedidoNoExisteDebeLanzarPedidoNoEncontradoException() {
    // Simula que el pedido NO existe (devuelve null)
    when(repositorioPedidoMock.buscarPedidoPorId(99L)).thenReturn(null);

    assertThrows(
      PedidoNoEncontradoException.class,
      () -> servicioPedido.actualizarEstadoPedido(99L, "ENTREGADO")
    );
  }
}
