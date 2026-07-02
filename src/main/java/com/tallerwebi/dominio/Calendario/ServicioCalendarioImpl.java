package com.tallerwebi.dominio.Calendario;

import com.tallerwebi.dominio.Pedidos.Pedido;
import com.tallerwebi.dominio.Pedidos.RepositorioPedido;
import com.tallerwebi.presentacion.Calendario.EventoCalendarioDTO;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicioCalendarioImpl implements ServicioCalendario {

  private final RepositorioPedido repositorioPedido;

  public ServicioCalendarioImpl(RepositorioPedido repositorioPedido) {
    this.repositorioPedido = repositorioPedido;
  }

  @Override
  public List<EventoCalendarioDTO> obtenerPedidosParaCalendarioDelUsuario(Long usuarioId) {
    List<Pedido> pedidos = repositorioPedido.obtenerTodosLosPedidosPorUsuario(usuarioId);
    List<EventoCalendarioDTO> eventos = new ArrayList<>();

    for (Pedido pedido : pedidos) {
      EventoCalendarioDTO evento = new EventoCalendarioDTO();
      evento.setPedidoId(pedido.getId());
      evento.setTitulo(pedido.getHijo().getNombre() + " #" + pedido.getId());

      // Convertimos el LocalDate a String de forma segura antes de enviarlo
      if (pedido.getFechaRetiro() != null) {
        evento.setFecha(pedido.getFechaRetiro().toString());
      }

      evento.setEstado(pedido.getEstado().name());
      evento.setClaseCss(pedido.getEstado().name());

      eventos.add(evento);
    }

    return eventos;
  }
}
