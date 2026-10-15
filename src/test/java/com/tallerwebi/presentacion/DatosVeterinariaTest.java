package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.tallerwebi.dominio.Servicio;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.Veterinaria;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.Test;

public class DatosVeterinariaTest {

  @Test
  public void crearVeterinariaDeberiaCopiarLosDatosYLasCredenciales() {
    DatosVeterinaria datos = datosValidos();

    Veterinaria veterinaria = datos.crearVeterinaria();

    assertThat(veterinaria.getNombre(), equalTo("Veterinaria Central"));
    assertThat(veterinaria.getCuit(), equalTo("30712345671"));
    assertThat(veterinaria.getUsuario().getEmail(), equalTo("vet@test.com"));
    assertThat(veterinaria.getUsuario().getPassword(), equalTo("secreto"));
    assertThat(veterinaria.getDiasAtencion(), contains(DayOfWeek.MONDAY, DayOfWeek.FRIDAY));
  }

  @Test
  public void convertirPerfilAFormularioDeberiaCopiarLosDatosExistentes() {
    Veterinaria veterinaria = datosValidos().crearVeterinaria();
    veterinaria.setHoraApertura(LocalTime.of(8, 30));
    Servicio servicio = new Servicio();
    servicio.setId(12L);
    veterinaria.getServicios().add(servicio);

    DatosVeterinaria datos = DatosVeterinaria.desde(veterinaria);

    assertThat(datos.getNombre(), equalTo("Veterinaria Central"));
    assertThat(datos.getEmail(), equalTo("vet@test.com"));
    assertThat(datos.getHoraApertura(), equalTo(LocalTime.of(8, 30)));
    assertThat(datos.getServiciosIds(), contains(12L));
    assertThat(datos.getPassword(), is((String) null));
  }

  @Test
  public void settersNulosDeColeccionesDeberianDejarlasVacias() {
    DatosVeterinaria datos = new DatosVeterinaria();
    datos.setDiasAtencion(null);
    datos.setServiciosIds(null);

    assertThat(datos.getDiasAtencion().isEmpty(), is(true));
    assertThat(datos.getServiciosIds().isEmpty(), is(true));
  }

  @Test
  public void crearDatosPerfilNoIncluyeUnaCuentaNueva() {
    DatosVeterinaria datos = datosValidos();

    assertThat(datos.crearDatosPerfil().getUsuario(), is((Usuario) null));
  }

  private DatosVeterinaria datosValidos() {
    DatosVeterinaria datos = new DatosVeterinaria();
    datos.setNombre("Veterinaria Central");
    datos.setCuit("30712345671");
    datos.setDireccion("Calle 123");
    datos.setTelefono("4444-1111");
    datos.setEmail("vet@test.com");
    datos.setPassword("secreto");
    datos.setHoraApertura(LocalTime.of(9, 0));
    datos.setHoraCierre(LocalTime.of(18, 0));
    datos.setDiasAtencion(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY));
    datos.setServiciosIds(List.of(12L));
    return datos;
  }
}
