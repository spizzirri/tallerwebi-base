package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.TurnoInvalido;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioTurnoTest {

  // "Hoy" es el miércoles 30/09/2026 a las 12:00.
  private static final LocalDate HOY = LocalDate.of(2026, 9, 30);
  private static final LocalDate JUEVES = HOY.plusDays(1);
  private static final LocalDate SABADO = HOY.plusDays(3);
  private static final LocalTime DIEZ = LocalTime.of(10, 0);

  private RepositorioTurno repositorioTurnoMock;
  private RepositorioVeterinaria repositorioVeterinariaMock;
  private RepositorioServicio repositorioServicioMock;
  private ServicioTurno servicioTurno;

  private Veterinaria veterinaria;
  private Servicio consulta;
  private Servicio cirugia;
  private Socio socio;
  private Mascota firulais;

  @BeforeEach
  public void init() {
    repositorioTurnoMock = mock(RepositorioTurno.class);
    repositorioVeterinariaMock = mock(RepositorioVeterinaria.class);
    repositorioServicioMock = mock(RepositorioServicio.class);
    Clock reloj = Clock.fixed(
      LocalDateTime.of(HOY, LocalTime.NOON).atZone(ZoneId.systemDefault()).toInstant(),
      ZoneId.systemDefault()
    );
    servicioTurno =
      new ServicioTurnoImpl(
        repositorioTurnoMock,
        repositorioVeterinariaMock,
        repositorioServicioMock,
        reloj
      );

    consulta = dadoUnServicio(1L, "Consulta clínica", 1);
    cirugia = dadoUnServicio(2L, "Cirugía", 2);
    veterinaria = dadaUnaVeterinariaQueAtiendeDe9A12(consulta, cirugia);
    firulais = dadaUnaMascotaConPlanBasico();
    socio = new Socio();
    socio.setId(1L);
    socio.agregarMascota(firulais);

    when(repositorioVeterinariaMock.buscarPorId(10L)).thenReturn(veterinaria);
    when(repositorioServicioMock.buscarPorId(1L)).thenReturn(consulta);
    when(repositorioServicioMock.buscarPorId(2L)).thenReturn(cirugia);
    when(repositorioTurnoMock.horasOcupadas(anyLong(), any())).thenReturn(List.of());
  }

  @Test
  public void horariosDisponiblesDeberiaDevolverLasFranjasDeMediaHoraDelHorarioDeAtencion() {
    List<LocalTime> horarios = servicioTurno.horariosDisponibles(veterinaria, JUEVES);

    assertThat(horarios, hasSize(6));
    assertThat(horarios.get(0), equalTo(LocalTime.of(9, 0)));
    assertThat(horarios.get(5), equalTo(LocalTime.of(11, 30)));
  }

  @Test
  public void horariosDisponiblesNoDeberiaIncluirLosYaReservados() {
    when(repositorioTurnoMock.horasOcupadas(10L, JUEVES)).thenReturn(List.of(DIEZ));

    List<LocalTime> horarios = servicioTurno.horariosDisponibles(veterinaria, JUEVES);

    assertThat(horarios, hasSize(5));
    assertThat(horarios, not(hasItem(DIEZ)));
  }

  @Test
  public void horariosDisponiblesDeHoyNoDeberiaIncluirHorariosQueYaPasaron() {
    List<LocalTime> horarios = servicioTurno.horariosDisponibles(veterinaria, HOY);

    assertThat(horarios, is(empty()));
  }

  @Test
  public void horariosDisponiblesDeberiaEstarVacioElFinDeSemanaYEnFechasPasadas() {
    assertThat(servicioTurno.horariosDisponibles(veterinaria, SABADO), is(empty()));
    assertThat(servicioTurno.horariosDisponibles(veterinaria, HOY.minusDays(1)), is(empty()));
  }

  @Test
  public void reservarUnTurnoValidoDeberiaGuardarloConfirmado() throws TurnoInvalido {
    Turno turno = servicioTurno.reservar(socio, dadaUnaSolicitud(1L, JUEVES, DIEZ));

    verify(repositorioTurnoMock, times(1)).guardar(turno);
    assertThat(turno.getEstado(), is(EstadoTurno.CONFIRMADO));
    assertThat(turno.getMascota(), is(firulais));
    assertThat(turno.getVeterinaria(), is(veterinaria));
    assertThat(turno.getServicio(), is(consulta));
    assertThat(turno.getFecha(), is(JUEVES));
    assertThat(turno.getHora(), is(DIEZ));
  }

  @Test
  public void reservarParaUnaMascotaQueNoEsDelSocioDeberiaFallar() {
    SolicitudTurno solicitud = dadaUnaSolicitud(1L, JUEVES, DIEZ);
    solicitud.setMascotaId(999L);

    assertThrows(TurnoInvalido.class, () -> servicioTurno.reservar(socio, solicitud));
    verify(repositorioTurnoMock, never()).guardar(any());
  }

  @Test
  public void reservarUnServicioQueNoCubreElPlanDeberiaFallar() {
    TurnoInvalido error = assertThrows(
      TurnoInvalido.class,
      () -> servicioTurno.reservar(socio, dadaUnaSolicitud(2L, JUEVES, DIEZ))
    );

    assertThat(error.getMessage(), equalTo("Cirugía no está incluido en el plan de Firulais"));
    verify(repositorioTurnoMock, never()).guardar(any());
  }

  @Test
  public void reservarUnServicioQueLaVeterinariaNoOfreceDeberiaFallar() {
    Servicio vacunacion = dadoUnServicio(3L, "Vacunación", 1);
    when(repositorioServicioMock.buscarPorId(3L)).thenReturn(vacunacion);

    assertThrows(
      TurnoInvalido.class,
      () -> servicioTurno.reservar(socio, dadaUnaSolicitud(3L, JUEVES, DIEZ))
    );
  }

  @Test
  public void reservarUnHorarioYaOcupadoDeberiaFallar() {
    when(repositorioTurnoMock.horasOcupadas(10L, JUEVES)).thenReturn(List.of(DIEZ));

    TurnoInvalido error = assertThrows(
      TurnoInvalido.class,
      () -> servicioTurno.reservar(socio, dadaUnaSolicitud(1L, JUEVES, DIEZ))
    );

    assertThat(error.getMessage(), equalTo("El horario elegido ya no está disponible"));
  }

  @Test
  public void reservarFueraDelHorarioDeAtencionDeberiaFallar() {
    assertThrows(
      TurnoInvalido.class,
      () -> servicioTurno.reservar(socio, dadaUnaSolicitud(1L, JUEVES, LocalTime.of(15, 0)))
    );
  }

  @Test
  public void reservarSinFechaDeberiaFallar() {
    assertThrows(
      TurnoInvalido.class,
      () -> servicioTurno.reservar(socio, dadaUnaSolicitud(1L, null, DIEZ))
    );
  }

  @Test
  public void turnosDeDeberiaDevolverLosTurnosDelSocio() {
    Turno turno = new Turno();
    when(repositorioTurnoMock.listarPorSocio(1L)).thenReturn(List.of(turno));

    assertThat(servicioTurno.turnosDe(socio), contains(turno));
  }

  @Test
  public void cancelarUnTurnoConfirmadoDelSocioDeberiaDejarloCancelado() throws TurnoInvalido {
    Turno turno = dadoUnTurnoDe(firulais);
    when(repositorioTurnoMock.buscarPorId(5L)).thenReturn(turno);

    servicioTurno.cancelar(socio, 5L);

    assertThat(turno.getEstado(), is(EstadoTurno.CANCELADO));
  }

  @Test
  public void cancelarUnTurnoDeOtroSocioDeberiaFallar() {
    Mascota ajena = new Mascota();
    ajena.setId(77L);
    when(repositorioTurnoMock.buscarPorId(5L)).thenReturn(dadoUnTurnoDe(ajena));

    assertThrows(TurnoInvalido.class, () -> servicioTurno.cancelar(socio, 5L));
  }

  @Test
  public void cancelarUnTurnoYaCanceladoDeberiaFallar() {
    Turno turno = dadoUnTurnoDe(firulais);
    turno.cancelar();
    when(repositorioTurnoMock.buscarPorId(5L)).thenReturn(turno);

    assertThrows(TurnoInvalido.class, () -> servicioTurno.cancelar(socio, 5L));
  }

  private Servicio dadoUnServicio(Long id, String nombre, int nivel) {
    Servicio servicio = new Servicio();
    servicio.setId(id);
    servicio.setNombre(nombre);
    servicio.setNivelRequerido(nivel);
    return servicio;
  }

  private Veterinaria dadaUnaVeterinariaQueAtiendeDe9A12(Servicio... servicios) {
    Veterinaria vet = new Veterinaria();
    vet.setId(10L);
    vet.setNombre("Veterinaria San Justo");
    vet.setHoraApertura(LocalTime.of(9, 0));
    vet.setHoraCierre(LocalTime.of(12, 0));
    vet.getServicios().addAll(List.of(servicios));
    return vet;
  }

  private Mascota dadaUnaMascotaConPlanBasico() {
    Plan basico = new Plan();
    basico.setNivel(1);
    Mascota mascota = new Mascota();
    mascota.setId(1L);
    mascota.setNombre("Firulais");
    mascota.setPlan(basico);
    return mascota;
  }

  private SolicitudTurno dadaUnaSolicitud(Long servicioId, LocalDate fecha, LocalTime hora) {
    SolicitudTurno solicitud = new SolicitudTurno();
    solicitud.setMascotaId(1L);
    solicitud.setVeterinariaId(10L);
    solicitud.setServicioId(servicioId);
    solicitud.setFecha(fecha);
    solicitud.setHora(hora);
    return solicitud;
  }

  private Turno dadoUnTurnoDe(Mascota mascota) {
    Turno turno = new Turno();
    turno.setMascota(mascota);
    return turno;
  }
}
