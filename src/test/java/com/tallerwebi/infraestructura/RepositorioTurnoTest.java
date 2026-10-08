package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.tallerwebi.dominio.Especie;
import com.tallerwebi.dominio.Mascota;
import com.tallerwebi.dominio.RepositorioTurno;
import com.tallerwebi.dominio.Servicio;
import com.tallerwebi.dominio.Socio;
import com.tallerwebi.dominio.Turno;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.Veterinaria;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
public class RepositorioTurnoTest {

  private static final LocalDate FECHA = LocalDate.of(2026, 10, 1);
  private static final LocalTime DIEZ = LocalTime.of(10, 0);

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioTurno repositorioTurno;
  private Veterinaria veterinaria;
  private Servicio servicio;

  @BeforeEach
  public void init() {
    repositorioTurno = new RepositorioTurnoImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void horasOcupadasDeberiaDevolverSoloLosTurnosConfirmadosDeEseDiaYVeterinaria() {
    Mascota mascota = dadoUnSocioConMascota("30111222");
    dadaUnaVeterinariaConServicio();
    guardarTurno(mascota, FECHA, DIEZ);
    Turno cancelado = guardarTurno(mascota, FECHA, LocalTime.of(11, 0));
    cancelado.cancelar();
    guardarTurno(mascota, FECHA.plusDays(1), LocalTime.of(12, 0));

    assertThat(repositorioTurno.horasOcupadas(veterinaria.getId(), FECHA), contains(DIEZ));
  }

  @Test
  @Transactional
  @Rollback
  public void listarPorSocioDeberiaDevolverSoloLosTurnosDeSusMascotas() {
    Mascota mia = dadoUnSocioConMascota("30111222");
    Mascota ajena = dadoUnSocioConMascota("30333444");
    dadaUnaVeterinariaConServicio();
    guardarTurno(mia, FECHA, DIEZ);
    guardarTurno(ajena, FECHA, LocalTime.of(11, 0));

    assertThat(repositorioTurno.listarPorSocio(mia.getSocio().getId()), hasSize(1));
  }

  @Test
  @Transactional
  public void sinTurnosNoHayHorasOcupadas() {
    assertThat(repositorioTurno.horasOcupadas(1L, FECHA), is(empty()));
  }

  private Session sesion() {
    return sessionFactory.getCurrentSession();
  }

  private Mascota dadoUnSocioConMascota(String dni) {
    Usuario usuario = new Usuario();
    usuario.setEmail(dni + "@test.com");
    Socio socio = new Socio();
    socio.setDni(dni);
    socio.setUsuario(usuario);
    Mascota mascota = new Mascota();
    mascota.setNombre("Firulais");
    mascota.setEspecie(Especie.PERRO);
    socio.agregarMascota(mascota);
    sesion().persist(socio);
    return mascota;
  }

  private void dadaUnaVeterinariaConServicio() {
    servicio = new Servicio();
    servicio.setNombre("Consulta clínica");
    servicio.setNivelRequerido(1);
    sesion().persist(servicio);
    veterinaria = new Veterinaria();
    veterinaria.setNombre("Veterinaria San Justo");
    veterinaria.getServicios().add(servicio);
    sesion().persist(veterinaria);
  }

  private Turno guardarTurno(Mascota mascota, LocalDate fecha, LocalTime hora) {
    Turno turno = new Turno();
    turno.setMascota(mascota);
    turno.setVeterinaria(veterinaria);
    turno.setServicio(servicio);
    turno.setFecha(fecha);
    turno.setHora(hora);
    repositorioTurno.guardar(turno);
    return turno;
  }

  @Test
  @Transactional
  @Rollback
  public void listarPorMascotaDeberiaDevolverSoloLosTurnosDeEsaMascota() {
    // dado
    Mascota mia = dadoUnSocioConMascota("30111222");
    Mascota ajena = dadoUnSocioConMascota("30333444");
    dadaUnaVeterinariaConServicio();
    guardarTurno(mia, FECHA, DIEZ);
    guardarTurno(mia, FECHA, LocalTime.of(11, 0));
    guardarTurno(ajena, FECHA, LocalTime.of(12, 0));

    List<Turno> turnos = repositorioTurno.listarPorMascota(mia.getId());

    assertThat(turnos, hasSize(2));
  }
}
