package com.tallerwebi.dominio.Pedido;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Hijos.Hijo;
import com.tallerwebi.dominio.Hijos.RepositorioHijo;
import com.tallerwebi.dominio.Pedidos.*;
import com.tallerwebi.dominio.Productos.Producto;
import com.tallerwebi.dominio.Productos.RepositorioProducto;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.dominio.excepcion.FechaRetiroInvalidaException;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import com.tallerwebi.dominio.excepcion.ProductoSinStockException;
import com.tallerwebi.presentacion.DistribucionCarrito.ItemDistribucionDTO;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    when(productoMock.getCantidad()).thenReturn(10);
    when(repositorioProductoMock.buscarProductoPorId(1L)).thenReturn(productoMock);

    List<ItemDistribucionDTO> items = List.of(new ItemDistribucionDTO(1L, 1L, 2));
    servicioPedido.crearPedido(1L, items, fechaRetiro, usuarioMock);
    verify(repositorioPedidoMock).guardar(any(Pedido.class));
  }

  @Test
  public void dadoUnHijoAlCrearPedidoElPedidoDebeTenerEseHijo() {
    when(hijoMock.getId()).thenReturn(1L);
    when(productoMock.getPrecio()).thenReturn(100.0);
    when(productoMock.getCantidad()).thenReturn(10);

    when(repositorioHijoMock.buscarPorId(1L)).thenReturn(hijoMock);
    when(repositorioProductoMock.buscarProductoPorId(1L)).thenReturn(productoMock);

    List<ItemDistribucionDTO> items = List.of(new ItemDistribucionDTO(1L, 1L, 2));

    servicioPedido.crearPedido(1L, items, fechaRetiro, usuarioMock);

    verify(repositorioPedidoMock).guardar(argThat(pedido -> pedido.getHijo() != null));
  }

  @Test
  public void dadoUnItemConCantidad2ElPedidoDebeCrearseConEsaCantidad() {
    when(productoMock.getPrecio()).thenReturn(100.0);
    when(productoMock.getCantidad()).thenReturn(10);
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

  @Test
  public void dadoUnIdDePedidoExistenteBuscarPorIdDebeRetornarElPedido() {
    Pedido pedidoMock = mock(Pedido.class);
    when(repositorioPedidoMock.buscarPedidoPorId(1L)).thenReturn(pedidoMock);

    Pedido resultado = servicioPedido.buscarPorId(1L);

    assertThat(resultado, notNullValue());
    assertThat(resultado, equalTo(pedidoMock));
  }

  @Test
  public void dadoUnIdDePedidoInexistenteBuscarPorIdDebeLanzarPedidoNoEncontradoException() {
    when(repositorioPedidoMock.buscarPedidoPorId(99L)).thenReturn(null);

    assertThrows(PedidoNoEncontradoException.class, () -> servicioPedido.buscarPorId(99L));
  }

  @Test
  public void dadoUnUsuarioConPedidosPendientesAlLimpiarDebeRestaurarStockYCambiarEstadoACancelado() {
    Pedido pedidoMock = mock(Pedido.class);
    ItemPedido itemMock = mock(ItemPedido.class);
    Producto productoMockReal = new Producto(); // Usamos un objeto real corto para verificar el cambio de stock sin encadenar tantos mocks
    productoMockReal.setCantidad(10);
    productoMockReal.setNombre("Alfajor");

    when(repositorioPedidoMock.obtenerPedidosPorUsuario(1L)).thenReturn(List.of(pedidoMock));
    when(pedidoMock.getItems()).thenReturn(List.of(itemMock));
    when(itemMock.getProducto()).thenReturn(productoMockReal);
    when(itemMock.getCantidad()).thenReturn(5);

    servicioPedido.limpiarPedidosPendientes(1L);

    // 10 iniciales + 5 restaurados = 15
    assertThat(productoMockReal.getCantidad(), equalTo(15));
    verify(pedidoMock).setEstado(EstadoPedido.CANCELADO);
  }

  @Test
  public void alActualizarPedidoExistenteDebeDevolverStockViejoDescontarNuevoYRecalcular()
    throws ProductoSinStockException {
    Pedido pedidoOriginal = new Pedido();
    Hijo hijoReal = new Hijo();
    hijoReal.setId(10L);
    pedidoOriginal.setHijo(hijoReal);

    Producto productoViejo = new Producto();
    productoViejo.setCantidad(5);
    productoViejo.setPrecio(100.0); // Seteamos precio para evitar NPE en calcularSubtotal

    ItemPedido itemViejo = new ItemPedido(productoViejo, 3);
    itemViejo.setPedido(pedidoOriginal);
    pedidoOriginal.agregarItem(itemViejo);

    // Configuración del pedido original a editar
    when(repositorioPedidoMock.buscarPedidoPorId(1L)).thenReturn(pedidoOriginal);
    // Producto nuevo al que se le va a re-descontar stock
    Producto productoNuevo = new Producto();
    productoNuevo.setCantidad(10);
    productoNuevo.setNombre("Jugo");
    productoNuevo.setPrecio(150.0); // Seteamos precio para evitar NPE en calcularSubtotal
    when(repositorioProductoMock.buscarProductoPorId(2L)).thenReturn(productoNuevo);
    ItemDistribucionDTO nuevoItemDTO = new ItemDistribucionDTO(2L, 10L, 4); // pide 4
    Map<Long, List<ItemDistribucionDTO>> listaPorHijo = Map.of(10L, List.of(nuevoItemDTO));

    // Ejecución
    servicioPedido.actualizarPedidoExistente(
      1L,
      listaPorHijo,
      LocalDate.now().plusDays(2),
      usuarioMock
    );

    // VERIFICACIONES:
    assertThat(productoViejo.getCantidad(), equalTo(8)); // 5 original + 3 devueltos
    assertThat(productoNuevo.getCantidad(), equalTo(6)); // 10 original - 4 pedidos
    assertThat(pedidoOriginal.getEstado(), equalTo(EstadoPedido.EN_CARRITO));
    verify(repositorioPedidoMock).guardar(pedidoOriginal);
  }

  @Test
  public void alActualizarPedidoExistenteSiNoHayStockSuficienteDebeLanzarProductoSinStockException() {
    // 1. Instanciamos objetos REALES
    Pedido pedidoOriginal = new Pedido();
    Hijo hijoReal = new Hijo();
    hijoReal.setId(10L);
    pedidoOriginal.setHijo(hijoReal);

    // Configuramos el mock para que devuelva nuestro pedido real
    when(repositorioPedidoMock.buscarPedidoPorId(1L)).thenReturn(pedidoOriginal);

    // Producto nuevo simulado que se queda sin stock (con cantidad, nombre y precio por las dudas)
    Producto productoNuevo = new Producto();
    productoNuevo.setCantidad(2);
    productoNuevo.setNombre("Galletitas");
    productoNuevo.setPrecio(50.0);
    when(repositorioProductoMock.buscarProductoPorId(2L)).thenReturn(productoNuevo);

    // Pedimos 5 unidades del producto 2 (supera las 2 disponibles)
    ItemDistribucionDTO nuevoItemDTO = new ItemDistribucionDTO(2L, 10L, 5);
    Map<Long, List<ItemDistribucionDTO>> listaPorHijo = Map.of(10L, List.of(nuevoItemDTO));

    // 4. VERIFICACIÓN: Comprobamos que el servicio lance SÍ O SÍ la excepción esperada
    assertThrows(
      ProductoSinStockException.class,
      () ->
        servicioPedido.actualizarPedidoExistente(
          1L,
          listaPorHijo,
          LocalDate.now().plusDays(2),
          usuarioMock
        )
    );
  }

  @Test
  public void alActualizarPedidoExistenteSiLaListaDeItemsVieneVaciaDebeCancelarElPedido()
    throws ProductoSinStockException {
    Pedido pedidoOriginal = mock(Pedido.class);
    Hijo hijoMock = mock(Hijo.class);

    when(repositorioPedidoMock.buscarPedidoPorId(1L)).thenReturn(pedidoOriginal);
    when(pedidoOriginal.getHijo()).thenReturn(hijoMock);
    when(hijoMock.getId()).thenReturn(10L);
    when(pedidoOriginal.getItems()).thenReturn(new ArrayList<>());

    // Pasamos un Map vacío (el usuario desmarcó todos los productos para este hijo)
    Map<Long, List<ItemDistribucionDTO>> listaPorHijo = Map.of();

    servicioPedido.actualizarPedidoExistente(
      1L,
      listaPorHijo,
      LocalDate.now().plusDays(2),
      usuarioMock
    );

    verify(pedidoOriginal).setEstado(EstadoPedido.CANCELADO);
    verify(pedidoOriginal).setSubtotal(0.0);
  }
}
