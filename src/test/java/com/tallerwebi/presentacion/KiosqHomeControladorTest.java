package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Hijos.Hijo;
import com.tallerwebi.dominio.Pedidos.EstadoPedido;
import com.tallerwebi.dominio.Pedidos.Pedido;
import com.tallerwebi.dominio.Pedidos.ServicioPedido;
import com.tallerwebi.dominio.Productos.Producto;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import com.tallerwebi.presentacion.Kiosquero.KiosqHomeControlador;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.hamcrest.Matchers;
import org.hamcrest.text.IsEqualIgnoringCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class KiosqHomeControladorTest {

  private KiosqHomeControlador kiosControlador;
  private HttpSession sessionMock;
  private Usuario usuarioMock;
  private Pedido pedidoMock;
  private Pedido pedidoMock2;
  private Hijo hijoMock;
  private ServicioPedido servicioPedidoMock;
  private RedirectAttributes redirectAttributesMock;

  @BeforeEach
  public void init() {
    servicioPedidoMock = Mockito.mock(ServicioPedido.class);
    kiosControlador = new KiosqHomeControlador(servicioPedidoMock);
    sessionMock = Mockito.mock(HttpSession.class);
    usuarioMock = Mockito.mock(Usuario.class);
    pedidoMock = Mockito.mock(Pedido.class);
    pedidoMock2 = Mockito.mock(Pedido.class);
    hijoMock = Mockito.mock(Hijo.class);
    redirectAttributesMock = Mockito.mock(RedirectAttributes.class);
  }

  @Test
  public void siNoHayKiosqueroLogueadoDebeVolverAlLogin() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

    ModelAndView mav = kiosControlador.irAlHomeKiosquero(sessionMock, null, null);

    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  @Test
  public void elHomeKiosqueroDebeMostrarNombreDeUsuario() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);

    when(sessionMock.getAttribute("ROL")).thenReturn("KIOSQUERO");
    when(usuarioMock.getNombre()).thenReturn("Rocio");

    ModelAndView mav = kiosControlador.irAlHomeKiosquero(sessionMock, null, null);
    assertThat(
      ((Usuario) mav.getModel().get("usuario")).getNombre(),
      IsEqualIgnoringCase.equalToIgnoringCase("Rocio")
    );
  }

  @Test
  public void elHomeKiosqueroDebeMostrarLosPedidosDeLosClientes() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(sessionMock.getAttribute("ROL")).thenReturn("KIOSQUERO");

    when(pedidoMock.getId()).thenReturn(1L);

    List<Pedido> pedidos = List.of(pedidoMock);
    when(servicioPedidoMock.obtenerPedidosDeLosUsuarios()).thenReturn(pedidos);
    ModelAndView mav = kiosControlador.irAlHomeKiosquero(sessionMock, null, null);
    List<Pedido> pedidosObtenidos = (List<Pedido>) mav.getModel().get("pedidosClientes");

    assertThat(pedidosObtenidos.get(0).getId(), equalTo(1L));
  }

  @Test
  public void seDebenVerLosPedidosFiltradosPorSuEstado() {
    String estado = "PAGADO";
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(sessionMock.getAttribute("ROL")).thenReturn("KIOSQUERO");

    when(pedidoMock.getId()).thenReturn(1L);
    when(pedidoMock.getEstado()).thenReturn(EstadoPedido.PAGADO);

    List<Pedido> pedidos = List.of(pedidoMock);
    when(servicioPedidoMock.obtenerPedidosDeLosUsuariosFiltrado(estado)).thenReturn(pedidos);

    ModelAndView mav = kiosControlador.irAlHomeKiosquero(sessionMock, estado, null);
    List<Pedido> pedidosObtenidos = (List<Pedido>) mav.getModel().get("pedidosClientes");

    assertThat(pedidosObtenidos.get(0).getId(), equalTo(1L));
    assertThat(pedidosObtenidos.get(0).getEstado(), equalTo(EstadoPedido.PAGADO));
  }

  @Test
  public void alBuscarUnPedidoPorSuNombreDeAlumnoDebeTraerLasCoincidencias() {
    String nombreAlumno = "Rocio";
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(sessionMock.getAttribute("ROL")).thenReturn("KIOSQUERO");

    when(hijoMock.getNombre()).thenReturn(nombreAlumno);

    when(pedidoMock.getId()).thenReturn(1L);
    when(pedidoMock.getEstado()).thenReturn(EstadoPedido.PAGADO);
    when(pedidoMock.getHijo()).thenReturn(hijoMock); // Evita el NullPointerException

    when(pedidoMock2.getId()).thenReturn(2L);
    when(pedidoMock2.getEstado()).thenReturn(EstadoPedido.PEDIDO_ARMADO);
    when(pedidoMock2.getHijo()).thenReturn(hijoMock); // Evita el NullPointerException

    List<Pedido> pedidos = List.of(pedidoMock, pedidoMock2);
    List<Pedido> listadoGeneralVacio = new ArrayList<>(); // Evita problemas en el listado de abajo

    when(servicioPedidoMock.obtenerPedidosDeLosUsuarios()).thenReturn(listadoGeneralVacio);
    when(servicioPedidoMock.obtenerResultadosBusquedaPorNombre(nombreAlumno)).thenReturn(pedidos);

    ModelAndView mav = kiosControlador.irAlHomeKiosquero(sessionMock, null, nombreAlumno);
    List<Pedido> pedidosObtenidos = (List<Pedido>) mav.getModel().get("pedidosBuscados");

    assertThat(pedidosObtenidos, hasSize(2));

    assertThat(pedidosObtenidos.get(0).getId(), equalTo(1L));
    assertThat(pedidosObtenidos.get(0).getEstado(), equalTo(EstadoPedido.PAGADO));
    assertThat(pedidosObtenidos.get(0).getHijo().getNombre(), equalTo(nombreAlumno));

    assertThat(pedidosObtenidos.get(1).getId(), equalTo(2L));
    assertThat(pedidosObtenidos.get(1).getEstado(), equalTo(EstadoPedido.PEDIDO_ARMADO));
    assertThat(pedidosObtenidos.get(1).getHijo().getNombre(), equalTo(nombreAlumno));
  }

  @Test
  public void alCambiarElEstadoDeUnPedidoDebeRedirigirAlHomeKiosqueroConMensajeDeExito() {
    Long idPedido = 1L;
    String estadoNuevo = "ENTREGADO";

    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(sessionMock.getAttribute("ROL")).thenReturn("KIOSQUERO");

    // Ejecutamos pasando el nuevo parámetro mockeado
    ModelAndView mav = kiosControlador.cambiarEstadoPedido(
      idPedido,
      estadoNuevo,
      sessionMock,
      redirectAttributesMock
    );

    // Verificamos que se haya invocado al servicio
    verify(servicioPedidoMock, times(1)).actualizarEstadoPedido(idPedido, estadoNuevo);

    // VERIFICACIÓN CLAVE: Que se haya guardado el flash attribute de éxito
    String mensajeEsperado =
      "¡El pedido #" + idPedido + " cambió al estado " + estadoNuevo + " con éxito!";
    verify(redirectAttributesMock, times(1)).addFlashAttribute("mensajeExito", mensajeEsperado);

    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/homeKiosquero"));
  }

  @Test
  public void siFallaElCambioDeEstadoDeUnPedidoDebeRedirigirAlHomeConMensajeDeError() {
    Long idPedido = 1L;
    String estadoNuevo = "ENTREGADO";
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(sessionMock.getAttribute("ROL")).thenReturn("KIOSQUERO");

    // Forzamos al servicio a lanzar una excepción simulando un fallo
    doThrow(new RuntimeException("Error de conexión con la BD"))
      .when(servicioPedidoMock)
      .actualizarEstadoPedido(idPedido, estadoNuevo);

    ModelAndView mav = kiosControlador.cambiarEstadoPedido(
      idPedido,
      estadoNuevo,
      sessionMock,
      redirectAttributesMock
    );

    // VERIFICACIÓN CLAVE: Que haya entrado al catch y guardado el flash attribute de error
    String mensajeErrorEsperado =
      "No se pudo cambiar el estado del pedido #" +
      idPedido +
      ". Error: Error de conexión con la BD";

    verify(redirectAttributesMock, times(1))
      .addFlashAttribute("mensajeError", mensajeErrorEsperado);

    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/homeKiosquero"));
  }

  @Test
  public void alBuscarUnPedidoPorIdNumericoDebeBuscarPorId() {
    String busquedaPorId = "123"; // Solo números
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(sessionMock.getAttribute("ROL")).thenReturn("KIOSQUERO");
    when(pedidoMock.getId()).thenReturn(123L);

    // Simulamos que el servicio encuentra el pedido exacto por ID
    when(servicioPedidoMock.obtenerResultadosBusquedaPedidoPorId(123L)).thenReturn(pedidoMock);

    ModelAndView mav = kiosControlador.irAlHomeKiosquero(sessionMock, null, busquedaPorId);
    List<Pedido> pedidosObtenidos = (List<Pedido>) mav.getModel().get("pedidosBuscados");

    assertThat(pedidosObtenidos, hasSize(1));
    assertThat(pedidosObtenidos.get(0).getId(), equalTo(123L));
  }

  @Test
  public void alBuscarUnPedidoSiElServicioLanzaPedidoNoEncontradoExceptionDebeCargarMensajeErrorEnElModelo() {
    String busquedaFallida = "AlumnoInexistente";
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(sessionMock.getAttribute("ROL")).thenReturn("KIOSQUERO");

    // Forzamos al servicio a lanzar la excepción específica que atrapa tu catch
    when(servicioPedidoMock.obtenerResultadosBusquedaPorNombre(busquedaFallida))
      .thenThrow(new PedidoNoEncontradoException("No se encontraron pedidos"));

    ModelAndView mav = kiosControlador.irAlHomeKiosquero(sessionMock, null, busquedaFallida);

    // Verificamos que se haya ejecutado el bloque catch y guardado el string del error
    assertThat(mav.getModel().get("errorBusquedaPedido"), equalTo("No se encontraron pedidos"));
  }

  @Test
  public void alCargarPedidosSiElServicioLanzaExcepcionDebeCapturarElErrorEnElModelo() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(sessionMock.getAttribute("ROL")).thenReturn("KIOSQUERO");

    // Forzamos la excepción en el listado general
    when(servicioPedidoMock.obtenerPedidosDeLosUsuarios())
      .thenThrow(new PedidoNoEncontradoException("Error general de pedidos"));

    ModelAndView mav = kiosControlador.irAlHomeKiosquero(sessionMock, null, null);

    assertThat(mav.getModel().get("errorBusquedaPedido"), equalTo("Error general de pedidos"));
  }

  @Test
  public void alCambiarEstadoSiElPedidoNoExisteDebeAtraparLaExcepcionYRedirigirConMensajeError() {
    Long idPedidoInexistente = 999L;
    String estadoNuevo = "PAGADO";

    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(sessionMock.getAttribute("ROL")).thenReturn("KIOSQUERO");

    // Forzamos al servicio a lanzar la excepción simulando que el pedido no se encontró
    doThrow(new PedidoNoEncontradoException("El pedido con ID 999 no existe."))
      .when(servicioPedidoMock)
      .actualizarEstadoPedido(idPedidoInexistente, estadoNuevo);

    ModelAndView mav = kiosControlador.cambiarEstadoPedido(
      idPedidoInexistente,
      estadoNuevo,
      sessionMock,
      redirectAttributesMock
    );

    // Verificamos que se guarde el Flash Attribute esperado por la vista en el bloque catch
    String mensajeErrorEsperado =
      "No se pudo cambiar el estado del pedido #999. Error: El pedido con ID 999 no existe.";
    verify(redirectAttributesMock, times(1))
      .addFlashAttribute("mensajeError", mensajeErrorEsperado);

    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/homeKiosquero"));
  }
}
