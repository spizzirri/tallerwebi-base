package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Hijos.Hijo;
import com.tallerwebi.dominio.Pedidos.EstadoPedido;
import com.tallerwebi.dominio.Pedidos.Pedido;
import com.tallerwebi.dominio.Pedidos.ServicioPedido;
import com.tallerwebi.dominio.Productos.Producto;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.presentacion.Kiosquero.KiosqHomeControlador;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.hamcrest.text.IsEqualIgnoringCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.ModelAndView;

public class KiosqHomeControladorTest {

  private KiosqHomeControlador kiosControlador;
  private HttpSession sessionMock;
  private Usuario usuarioMock;
  private Pedido pedidoMock;
  private Pedido pedidoMock2;
  private Hijo hijoMock;
  private ServicioPedido servicioPedidoMock;

  @BeforeEach
  public void init() {
    servicioPedidoMock = Mockito.mock(ServicioPedido.class);
    kiosControlador = new KiosqHomeControlador(servicioPedidoMock);
    sessionMock = Mockito.mock(HttpSession.class);
    usuarioMock = Mockito.mock(Usuario.class);
    pedidoMock = Mockito.mock(Pedido.class);
    pedidoMock2 = Mockito.mock(Pedido.class);
    hijoMock = Mockito.mock(Hijo.class);
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

    assertThat(pedidosObtenidos, org.hamcrest.Matchers.hasSize(2));

    assertThat(pedidosObtenidos.get(0).getId(), equalTo(1L));
    assertThat(pedidosObtenidos.get(0).getEstado(), equalTo(EstadoPedido.PAGADO));
    assertThat(pedidosObtenidos.get(0).getHijo().getNombre(), equalTo(nombreAlumno));

    assertThat(pedidosObtenidos.get(1).getId(), equalTo(2L));
    assertThat(pedidosObtenidos.get(1).getEstado(), equalTo(EstadoPedido.PEDIDO_ARMADO));
    assertThat(pedidosObtenidos.get(1).getHijo().getNombre(), equalTo(nombreAlumno));
  }
}
