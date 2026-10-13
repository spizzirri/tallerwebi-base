package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.TurnoInvalido;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ServicioTurno {
  List<LocalTime> horariosDisponibles(Veterinaria veterinaria, LocalDate fecha);
  Turno reservar(Socio socio, SolicitudTurno solicitud) throws TurnoInvalido;
  Turno buscarTurnoDe(Socio socio, Long turnoId) throws TurnoInvalido;
  List<Turno> turnosDe(Socio socio);
  void cancelar(Socio socio, Long turnoId) throws TurnoInvalido;
  void reprogramar(Socio socio, Long turnoId, LocalDate diaNuevo, LocalTime horaNueva)
    throws TurnoInvalido;
  HistorialTurnos historialDe(Socio socio, Long mascotaId) throws TurnoInvalido;
}
