package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.TurnoInvalido;
import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioTurno")
@Transactional
public class ServicioTurnoImpl implements ServicioTurno {

  private final RepositorioTurno repositorioTurno;
  private final RepositorioVeterinaria repositorioVeterinaria;
  private final RepositorioServicio repositorioServicio;
  private final Clock reloj;

  @Autowired
  public ServicioTurnoImpl(
    RepositorioTurno repositorioTurno,
    RepositorioVeterinaria repositorioVeterinaria,
    RepositorioServicio repositorioServicio
  ) {
    this(repositorioTurno, repositorioVeterinaria, repositorioServicio, Clock.systemDefaultZone());
  }

  // Permite fijar "hoy" en los tests.
  public ServicioTurnoImpl(
    RepositorioTurno repositorioTurno,
    RepositorioVeterinaria repositorioVeterinaria,
    RepositorioServicio repositorioServicio,
    Clock reloj
  ) {
    this.repositorioTurno = repositorioTurno;
    this.repositorioVeterinaria = repositorioVeterinaria;
    this.repositorioServicio = repositorioServicio;
    this.reloj = reloj;
  }

  @Override
  public List<LocalTime> horariosDisponibles(Veterinaria veterinaria, LocalDate fecha) {
    LocalDate hoy = LocalDate.now(reloj);
    if (
      veterinaria == null || fecha == null || fecha.isBefore(hoy) || !veterinaria.atiendeEl(fecha)
    ) {
      return List.of();
    }
    List<LocalTime> ocupadas = repositorioTurno.horasOcupadas(veterinaria.getId(), fecha);
    LocalTime ahora = LocalTime.now(reloj);
    return veterinaria
      .horariosDeAtencion()
      .stream()
      .filter(h -> !ocupadas.contains(h))
      .filter(h -> fecha.isAfter(hoy) || h.isAfter(ahora))
      .collect(Collectors.toList());
  }

  @Override
  public Turno reservar(Socio socio, SolicitudTurno solicitud) throws TurnoInvalido {
    Mascota mascota = socio.buscarMascota(solicitud.getMascotaId());
    if (mascota == null) {
      throw new TurnoInvalido("Elegí una de tus mascotas");
    }
    Veterinaria veterinaria = repositorioVeterinaria.buscarPorId(solicitud.getVeterinariaId());
    Servicio servicio = repositorioServicio.buscarPorId(solicitud.getServicioId());
    validarServicio(mascota, veterinaria, servicio);
    validarHorario(veterinaria, solicitud.getFecha(), solicitud.getHora());

    Turno turno = new Turno();
    turno.setMascota(mascota);
    turno.setVeterinaria(veterinaria);
    turno.setServicio(servicio);
    turno.setFecha(solicitud.getFecha());
    turno.setHora(solicitud.getHora());
    turno.setObservaciones(solicitud.getObservaciones());
    repositorioTurno.guardar(turno);
    return turno;
  }

  private void validarServicio(Mascota mascota, Veterinaria veterinaria, Servicio servicio)
    throws TurnoInvalido {
    if (veterinaria == null || servicio == null) {
      throw new TurnoInvalido("Elegí una veterinaria y un servicio");
    }
    if (!veterinaria.ofrece(servicio)) {
      throw new TurnoInvalido(veterinaria.getNombre() + " no ofrece " + servicio.getNombre());
    }
    if (!mascota.tieneCubierto(servicio)) {
      throw new TurnoInvalido(
        servicio.getNombre() + " no está incluido en el plan de " + mascota.getNombre()
      );
    }
  }

  private void validarHorario(Veterinaria veterinaria, LocalDate fecha, LocalTime hora)
    throws TurnoInvalido {
    if (fecha == null || hora == null) {
      throw new TurnoInvalido("Elegí fecha y horario");
    }
    if (!horariosDisponibles(veterinaria, fecha).contains(hora)) {
      throw new TurnoInvalido("El horario elegido ya no está disponible");
    }
  }

  @Override
  public List<Turno> turnosDe(Socio socio) {
    return repositorioTurno.listarPorSocio(socio.getId());
  }

  @Override
  public void cancelar(Socio socio, Long turnoId) throws TurnoInvalido {
    Turno turno = turnoId == null ? null : repositorioTurno.buscarPorId(turnoId);
    if (turno == null || socio.buscarMascota(turno.getMascota().getId()) == null) {
      throw new TurnoInvalido("No encontramos ese turno");
    }
    if (!turno.estaConfirmado()) {
      throw new TurnoInvalido("Solo se pueden cancelar turnos confirmados");
    }
    turno.cancelar();
  }
}
