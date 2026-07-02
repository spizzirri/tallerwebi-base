package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Calendario.ServicioCalendario;
import com.tallerwebi.dominio.Calendario.ServicioCalendarioImpl;
import com.tallerwebi.dominio.Pedidos.EstadoPedido;
import com.tallerwebi.dominio.Pedidos.Pedido;
import com.tallerwebi.dominio.Pedidos.RepositorioPedido;
import com.tallerwebi.presentacion.Calendario.EventoCalendarioDTO;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ServicioCalendarioTest {

  private ServicioCalendario servicioCalendario;
  private RepositorioPedido repositorioPedidoMock;

  @BeforeEach
  public void init() {
    repositorioPedidoMock = Mockito.mock(RepositorioPedido.class);
    servicioCalendario = new ServicioCalendarioImpl(repositorioPedidoMock);
  }

  @Test
  public void debeRetornarListaVaciaSiElUsuarioNoTienePedidos() {
    when(repositorioPedidoMock.obtenerTodosLosPedidosPorUsuario(1L))
      .thenReturn(Collections.emptyList());
    List<EventoCalendarioDTO> eventos = servicioCalendario.obtenerPedidosParaCalendarioDelUsuario(
      1L
    );

    assertThat(eventos.size(), equalTo(0));
  }

  @Test
  public void debeRetornarLosPedidosDelUsuarioParaElCalendario() {
    Pedido pedido = new Pedido();

    pedido.setId(1L);
    pedido.setFechaRetiro(LocalDate.of(2026, 7, 10));
    pedido.setEstado(EstadoPedido.PAGADO);
    when(repositorioPedidoMock.obtenerTodosLosPedidosPorUsuario(1L)).thenReturn(List.of(pedido));
  }
}
