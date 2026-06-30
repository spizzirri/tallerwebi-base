package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.tallerwebi.dominio.Hijos.Curso;
import com.tallerwebi.dominio.Hijos.Hijo;
import com.tallerwebi.dominio.Pedidos.EstadoPedido;
import com.tallerwebi.dominio.Pedidos.ItemPedido;
import com.tallerwebi.dominio.Pedidos.Pedido;
import com.tallerwebi.dominio.Pedidos.RepositorioPedido;
import com.tallerwebi.dominio.Productos.Producto;
import com.tallerwebi.dominio.Usuario.DatosPersonales;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import java.time.LocalDate;
import java.util.List;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
public class RepositorioPedidoTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioPedido repositorioPedido;

  @BeforeEach
  public void init() {
    repositorioPedido = new RepositorioPedidoImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void dadoUnPedidoAlGuardarloDebePersistitrseEnLaBaseDeDatos() {
    Usuario usuario = dadoQueExisteUnUsuario();
    Hijo hijo = dadoQueExisteUnHijo(usuario);
    Producto producto = dadoQueExisteUnProducto();

    Pedido pedido = new Pedido();
    pedido.setUsuario(usuario);
    pedido.setHijo(hijo);
    pedido.setFechaRetiro(LocalDate.now().plusDays(1));

    ItemPedido itemPedido = new ItemPedido(producto, 2);
    itemPedido.setPedido(pedido);
    pedido.agregarItem(itemPedido);
    pedido.calcularSubtotal();

    repositorioPedido.guardar(pedido);

    Pedido obtenido = sessionFactory.getCurrentSession().get(Pedido.class, pedido.getId());

    assertThat(obtenido, notNullValue());
    assertThat(obtenido.getUsuario().getId(), equalTo(usuario.getId()));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoUnUsuarioConPedidosDebeRetornarSusPedidos() {
    Usuario usuario = dadoQueExisteUnUsuario();
    Hijo hijo = dadoQueExisteUnHijo(usuario);
    Producto producto = dadoQueExisteUnProducto();

    Pedido pedido = new Pedido();
    pedido.setUsuario(usuario);
    pedido.setHijo(hijo);
    pedido.setFechaRetiro(LocalDate.now().plusDays(1));

    ItemPedido itemPedido = new ItemPedido(producto, 2);
    itemPedido.setPedido(pedido);

    pedido.agregarItem(itemPedido);
    pedido.calcularSubtotal();
    sessionFactory.getCurrentSession().save(pedido);

    List<Pedido> pedidos = repositorioPedido.obtenerPedidosPorUsuario(usuario.getId());

    assertThat(pedidos, hasSize(1));
    assertThat(pedidos.get(0).getHijo().getNombre(), equalTo("Santi"));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoUnUsuarioConPedidosPreviosAlGenerarUnoNuevoElPendienteDebeSerCancelado() {
    Usuario usuario = dadoQueExisteUnUsuario();
    Hijo hijo = dadoQueExisteUnHijo(usuario);
    Producto producto = dadoQueExisteUnProducto();
    Pedido pedido = new Pedido();
    pedido.setUsuario(usuario);
    pedido.setHijo(hijo);
    pedido.setFechaRetiro(LocalDate.now().plusDays(1));

    ItemPedido itemPedido = new ItemPedido(producto, 2);
    itemPedido.setPedido(pedido);

    pedido.agregarItem(itemPedido);
    pedido.calcularSubtotal();
    pedido.setEstado(EstadoPedido.PAGO_PENDIENTE); // ← esto falta
    pedido.setFechaRetiro(LocalDate.now().plusDays(1));

    sessionFactory.getCurrentSession().save(pedido);

    repositorioPedido.eliminarPedidosPendientes(usuario.getId());
    // Limpiamos el caché de la sesión para forzar que vuelva a leer de la BD
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    List<Pedido> pedidosPendiente = repositorioPedido.obtenerPedidosPorUsuario(usuario.getId());
    assertThat(pedidosPendiente, hasSize(0));

    Pedido pedidoActualizado = sessionFactory.getCurrentSession().get(Pedido.class, pedido.getId());
    assertThat(pedidoActualizado.getEstado(), equalTo(EstadoPedido.CANCELADO));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoUnPedidoConPagoPendienteLuegoDeSerPagadoDebeCambiarSuEstadoAPagado() {
    Usuario usuario = dadoQueExisteUnUsuario();
    Hijo hijo = dadoQueExisteUnHijo(usuario);
    Producto producto = dadoQueExisteUnProducto();
    Pedido pedido = new Pedido();
    pedido.setUsuario(usuario);
    pedido.setHijo(hijo);

    ItemPedido itemPedido = new ItemPedido(producto, 2);
    itemPedido.setPedido(pedido);

    pedido.agregarItem(itemPedido);
    pedido.calcularSubtotal();
    pedido.setEstado(EstadoPedido.PAGO_PENDIENTE);
    pedido.setFechaRetiro(LocalDate.now().plusDays(1));

    sessionFactory.getCurrentSession().save(pedido);

    repositorioPedido.marcarPedidoPagado(usuario.getId());
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    Pedido pedidoActualizado = sessionFactory.getCurrentSession().get(Pedido.class, pedido.getId());
    assertThat(pedidoActualizado.getEstado(), equalTo(EstadoPedido.PAGADO));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoQueTengoUnUsuarioConPedidosDeboPoderVerSusPedidos() {
    Usuario usuario = dadoQueExisteUnUsuario();
    Hijo hijo = dadoQueExisteUnHijo(usuario);
    Producto producto = dadoQueExisteUnProducto();
    Pedido pedido = new Pedido();
    pedido.setUsuario(usuario);
    pedido.setHijo(hijo);
    pedido.setFechaRetiro(LocalDate.now().plusDays(1));

    ItemPedido itemPedido = new ItemPedido(producto, 2);
    itemPedido.setPedido(pedido);

    pedido.agregarItem(itemPedido);
    pedido.calcularSubtotal();
    pedido.setEstado(EstadoPedido.PAGADO);

    sessionFactory.getCurrentSession().save(pedido);
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    List<Pedido> pedidos = repositorioPedido.obtenerTodosLosPedidosPorUsuario(usuario.getId());

    assertThat(pedidos, hasSize(1));
  }

  //METODOS PARA EL USUARIO KIOSQUERO
  @Test
  @Transactional
  @Rollback
  public void dadoUnKiosqueroDeboPoderVerTodosLosPedidosPorUsuario() {
    // GIVEN (Dado que existen datos en la BD)
    Usuario usuario = dadoQueExisteUnUsuario();
    Hijo hijo = dadoQueExisteUnHijo(usuario);
    Producto producto = dadoQueExisteUnProducto();

    Pedido pedido = new Pedido();
    pedido.setUsuario(usuario);
    pedido.setHijo(hijo);
    pedido.setEstado(EstadoPedido.PAGADO);

    ItemPedido itemPedido = new ItemPedido(producto, 2);
    itemPedido.setPedido(pedido);
    pedido.agregarItem(itemPedido);
    pedido.calcularSubtotal();

    // Guardamos en la base de datos directamente usando Hibernate para testear la lectura posterior
    sessionFactory.getCurrentSession().save(pedido);

    // Forzamos la escritura y limpiamos la caché para asegurar que se pruebe la query HQL real
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    // WHEN (Cuando el kiosquero pide todos los pedidos)
    List<Pedido> pedidosObtenidos = repositorioPedido.obtenerTodosLosPedidosDeTodosLosClientes();

    // THEN (Entonces debe recuperar el pedido)
    assertThat(pedidosObtenidos, hasSize(1));
    assertThat(pedidosObtenidos.get(0).getId(), equalTo(pedido.getId()));
    assertThat(pedidosObtenidos.get(0).getHijo().getNombre(), equalTo("Santi"));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoUnKiosqueroDeboPoderVerTodosLosPedidosPorUsuarioFiltradoPorEstado() {
    // GIVEN (Dado que existen múltiples pedidos con distintos estados)
    Usuario usuario = dadoQueExisteUnUsuario();
    Hijo hijo = dadoQueExisteUnHijo(usuario);
    Producto producto = dadoQueExisteUnProducto();

    // Pedido 1: Estado PAGADO
    Pedido pedidoPagado = new Pedido();
    pedidoPagado.setUsuario(usuario);
    pedidoPagado.setHijo(hijo);
    pedidoPagado.setEstado(EstadoPedido.PAGADO);
    ItemPedido item1 = new ItemPedido(producto, 1);
    item1.setPedido(pedidoPagado);
    pedidoPagado.agregarItem(item1);
    pedidoPagado.calcularSubtotal();
    sessionFactory.getCurrentSession().save(pedidoPagado);

    // Pedido 2: Estado CANCELADO (No debería venir en el filtro de 'PAGADO')
    Pedido pedidoCancelado = new Pedido();
    pedidoCancelado.setUsuario(usuario);
    pedidoCancelado.setHijo(hijo);
    pedidoCancelado.setEstado(EstadoPedido.CANCELADO);
    ItemPedido item2 = new ItemPedido(producto, 1);
    item2.setPedido(pedidoCancelado);
    pedidoCancelado.agregarItem(item2);
    pedidoCancelado.calcularSubtotal();
    sessionFactory.getCurrentSession().save(pedidoCancelado);

    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    // WHEN (Cuando el kiosquero filtra únicamente por "PAGADO")
    List<Pedido> pedidosFiltrados =
      repositorioPedido.obtenerTodosLosPedidosDeTodosLosClientesFiltrado("PAGADO");

    // THEN (Entonces solo debe retornar el pedido que coincida con ese estado)
    assertThat(pedidosFiltrados, hasSize(1));
    assertThat(pedidosFiltrados.get(0).getEstado(), equalTo(EstadoPedido.PAGADO));
    assertThat(pedidosFiltrados.get(0).getId(), equalTo(pedidoPagado.getId()));
  }

  //METODOS AUXILIARES
  private Usuario dadoQueExisteUnUsuario() {
    DatosPersonales datos = new DatosPersonales();
    datos.setNombre("Juan");
    datos.setApellido("Perez");
    datos.setDni(12345678L);
    datos.setCelular(1122334455L);

    Usuario usuario = new Usuario();
    usuario.setDatosPersonales(datos);
    usuario.setEmail("juan@test.com");
    usuario.setPassword("1234");
    usuario.setRol("CLIENTE");

    sessionFactory.getCurrentSession().save(usuario);
    return usuario;
  }

  private Hijo dadoQueExisteUnHijo(Usuario padre) {
    Hijo hijo = new Hijo();
    hijo.setNombre("Santi");
    hijo.setDni(99999L);
    hijo.setPadre(padre);
    hijo.setApellido("Perez");
    hijo.setCurso(Curso.CUARTO_D);

    sessionFactory.getCurrentSession().save(hijo);
    return hijo;
  }

  private Producto dadoQueExisteUnProducto() {
    Producto producto = new Producto();
    producto.setNombre("Alfajor");
    producto.setPrecio(100.0);
    producto.setCantidad(10);
    sessionFactory.getCurrentSession().save(producto);
    return producto;
  }
}
