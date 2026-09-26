package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.DatosDeRegistroInvalidos;
import com.tallerwebi.dominio.excepcion.SocioExistente;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioRegistroTest {

  private ServicioRegistro servicioRegistro;
  private RepositorioSocio repositorioSocioMock;
  private RepositorioUsuario repositorioUsuarioMock;

  @BeforeEach
  public void init() {
    repositorioSocioMock = mock(RepositorioSocio.class);
    repositorioUsuarioMock = mock(RepositorioUsuario.class);
    servicioRegistro = new ServicioRegistroImpl(repositorioSocioMock, repositorioUsuarioMock);
  }

  @Test
  public void registrarUnSocioValidoDeberiaGuardarloConSuMascota() throws Exception {
    Socio socio = dadoUnSocioValido();
    Mascota mascota = dadaUnaMascotaValida();

    servicioRegistro.registrarSocio(socio, mascota);

    verify(repositorioSocioMock, times(1)).guardar(socio);
    assertThat(socio.getMascotas(), contains(mascota));
    assertThat(mascota.getSocio(), is(socio));
  }

  @Test
  public void registrarUnSocioDeberiaDejarSuUsuarioActivoYConRolSocio() throws Exception {
    Socio socio = dadoUnSocioValido();

    servicioRegistro.registrarSocio(socio, dadaUnaMascotaValida());

    assertThat(socio.getUsuario().getRol(), equalTo("SOCIO"));
    assertThat(socio.getUsuario().getActivo(), is(true));
  }

  @Test
  public void registrarUnSocioConEmailYaUsadoDeberiaLanzarUsuarioExistente() {
    Socio socio = dadoUnSocioValido();
    when(repositorioUsuarioMock.buscar(socio.getUsuario().getEmail())).thenReturn(new Usuario());

    assertThrows(
      UsuarioExistente.class,
      () -> servicioRegistro.registrarSocio(socio, dadaUnaMascotaValida())
    );
    verify(repositorioSocioMock, never()).guardar(any());
  }

  @Test
  public void registrarUnSocioConDniYaUsadoDeberiaLanzarSocioExistente() {
    Socio socio = dadoUnSocioValido();
    when(repositorioSocioMock.buscarPorDni(socio.getDni())).thenReturn(new Socio());

    assertThrows(
      SocioExistente.class,
      () -> servicioRegistro.registrarSocio(socio, dadaUnaMascotaValida())
    );
    verify(repositorioSocioMock, never()).guardar(any());
  }

  @Test
  public void registrarUnSocioSinMascotaDeberiaLanzarDatosInvalidos() {
    assertThrows(
      DatosDeRegistroInvalidos.class,
      () -> servicioRegistro.registrarSocio(dadoUnSocioValido(), null)
    );
  }

  @Test
  public void registrarUnaMascotaSinNombreDeberiaLanzarDatosInvalidos() {
    Mascota mascota = dadaUnaMascotaValida();
    mascota.setNombre(" ");

    assertThrows(
      DatosDeRegistroInvalidos.class,
      () -> servicioRegistro.registrarSocio(dadoUnSocioValido(), mascota)
    );
  }

  @Test
  public void registrarUnaMascotaSinEspecieDeberiaLanzarDatosInvalidos() {
    Mascota mascota = dadaUnaMascotaValida();
    mascota.setEspecie(null);

    assertThrows(
      DatosDeRegistroInvalidos.class,
      () -> servicioRegistro.registrarSocio(dadoUnSocioValido(), mascota)
    );
  }

  @Test
  public void registrarUnaMascotaConPesoNoPositivoDeberiaLanzarDatosInvalidos() {
    Mascota mascota = dadaUnaMascotaValida();
    mascota.setPeso(0.0);

    assertThrows(
      DatosDeRegistroInvalidos.class,
      () -> servicioRegistro.registrarSocio(dadoUnSocioValido(), mascota)
    );
  }

  @Test
  public void registrarUnaMascotaConFechaDeNacimientoFuturaDeberiaLanzarDatosInvalidos() {
    Mascota mascota = dadaUnaMascotaValida();
    mascota.setFechaNacimiento(LocalDate.now().plusDays(1));

    assertThrows(
      DatosDeRegistroInvalidos.class,
      () -> servicioRegistro.registrarSocio(dadoUnSocioValido(), mascota)
    );
  }

  @Test
  public void registrarUnSocioSinApellidoDeberiaLanzarDatosInvalidos() {
    Socio socio = dadoUnSocioValido();
    socio.setApellido("");

    assertThrows(
      DatosDeRegistroInvalidos.class,
      () -> servicioRegistro.registrarSocio(socio, dadaUnaMascotaValida())
    );
  }

  @Test
  public void registrarUnSocioConDniConLetrasDeberiaLanzarDatosInvalidos() {
    Socio socio = dadoUnSocioValido();
    socio.setDni("30ABC456");

    assertThrows(
      DatosDeRegistroInvalidos.class,
      () -> servicioRegistro.registrarSocio(socio, dadaUnaMascotaValida())
    );
  }

  @Test
  public void registrarUnSocioSinPasswordDeberiaLanzarDatosInvalidos() {
    Socio socio = dadoUnSocioValido();
    socio.getUsuario().setPassword("");

    assertThrows(
      DatosDeRegistroInvalidos.class,
      () -> servicioRegistro.registrarSocio(socio, dadaUnaMascotaValida())
    );
  }

  private Socio dadoUnSocioValido() {
    Usuario usuario = new Usuario();
    usuario.setEmail("juan@test.com");
    usuario.setPassword("123456");

    Socio socio = new Socio();
    socio.setNombre("Juan");
    socio.setApellido("Pérez");
    socio.setDni("30123456");
    socio.setTelefono("1122334455");
    socio.setUsuario(usuario);
    return socio;
  }

  private Mascota dadaUnaMascotaValida() {
    Mascota mascota = new Mascota();
    mascota.setNombre("Firulais");
    mascota.setEspecie(Especie.PERRO);
    mascota.setRaza("Mestizo");
    mascota.setFechaNacimiento(LocalDate.of(2020, 5, 10));
    mascota.setPeso(12.5);
    return mascota;
  }
}
