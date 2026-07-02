package com.tallerwebi.dominio.Calendario;

import com.tallerwebi.presentacion.Calendario.EventoCalendarioDTO;
import java.util.List;

public interface ServicioCalendario {
  List<EventoCalendarioDTO> obtenerPedidosParaCalendarioDelUsuario(Long usuarioId);
}
