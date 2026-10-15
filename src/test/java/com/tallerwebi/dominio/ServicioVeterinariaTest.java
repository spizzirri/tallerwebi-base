package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.DatosDeRegistroInvalidos;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioVeterinariaTest {

  private ServicioVeterinaria servicioVeterinaria;
  private RepositorioServicio repositorioServicioMock;
  private RepositorioVeterinaria repositorioVeterinariaMock;
  private RepositorioUsuario repositorioUsuarioMock;
  private Veterinaria conCirugia;
  private Veterinaria sinCirugia;
  private Servicio cirugia;

  @BeforeEach
  public void init() {
    repositorioVeterinariaMock = mock(RepositorioVeterinaria.class);
    repositorioServicioMock = mock(RepositorioServicio.class);
    repositorioUsuarioMock = mock(RepositorioUsuario.class);
    servicioVeterinaria =
      new ServicioVeterinariaImpl(
        repositorioVeterinariaMock,
        repositorioServicioMock,
        repositorioUsuarioMock
      );

    cirugia = new Servicio();
    cirugia.setId(13L);
    conCirugia = new Veterinaria();
    conCirugia.getServicios().add(cirugia);
    sinCirugia = new Veterinaria();
    when(repositorioVeterinariaMock.listar()).thenReturn(List.of(conCirugia, sinCirugia));
  }

  @Test
  public void listarSinFiltroDeberiaDevolverTodasLasVeterinarias() {
    assertThat(servicioVeterinaria.listar(null), contains(conCirugia, sinCirugia));
  }

  @Test
  public void listarPorServicioDeberiaDevolverSoloLasQueLoOfrecen() {
    when(repositorioServicioMock.buscarPorId(13L)).thenReturn(cirugia);

    assertThat(servicioVeterinaria.listar(13L), contains(conCirugia));
  }

  @Test
  public void registrarVeterinariaDeberiaActivarLaCuentaYGuardarSuPerfil() throws Exception {
    Veterinaria veterinaria = datosRegistroValidos();
    when(repositorioServicioMock.buscarPorId(13L)).thenReturn(cirugia);

    servicioVeterinaria.registrar(veterinaria, List.of(13L));

    verify(repositorioVeterinariaMock).guardar(veterinaria);
    assertThat(veterinaria.getCuit(), equalTo("30712345671"));
    assertThat(veterinaria.getUsuario().getEmail(), equalTo("vet@test.com"));
    assertThat(veterinaria.getUsuario().getRol(), equalTo(Usuario.ROL_VETERINARIA));
    assertThat(veterinaria.getUsuario().getActivo(), is(true));
    assertThat(veterinaria.getServicios(), contains(cirugia));
  }

  @Test
  public void registrarVeterinariaConEmailExistenteDeberiaRechazarla() {
    Veterinaria veterinaria = datosRegistroValidos();
    when(repositorioUsuarioMock.buscar("vet@test.com")).thenReturn(new Usuario());

    assertThrows(
      UsuarioExistente.class,
      () -> servicioVeterinaria.registrar(veterinaria, List.of(13L))
    );
    verify(repositorioVeterinariaMock, never()).guardar(any());
  }

  @Test
  public void registrarVeterinariaConCuitExistenteDeberiaRechazarla() {
    Veterinaria veterinaria = datosRegistroValidos();
    when(repositorioVeterinariaMock.buscarPorCuit("30712345671")).thenReturn(new Veterinaria());

    DatosDeRegistroInvalidos error = assertThrows(
      DatosDeRegistroInvalidos.class,
      () -> servicioVeterinaria.registrar(veterinaria, List.of(13L))
    );

    assertThat(error.getMessage(), equalTo("Ya existe una veterinaria con ese CUIT/CUIL"));
  }

  @Test
  public void registrarVeterinariaSinDiasDeAtencionDeberiaRechazarla() {
    Veterinaria veterinaria = datosRegistroValidos();
    veterinaria.setDiasAtencion(EnumSet.noneOf(DayOfWeek.class));

    assertThrows(
      DatosDeRegistroInvalidos.class,
      () -> servicioVeterinaria.registrar(veterinaria, List.of(13L))
    );
  }

  @Test
  public void registrarVeterinariaSinServicioValidoDeberiaRechazarla() {
    Veterinaria veterinaria = datosRegistroValidos();
    when(repositorioServicioMock.buscarPorId(13L)).thenReturn(null);

    assertThrows(
      DatosDeRegistroInvalidos.class,
      () -> servicioVeterinaria.registrar(veterinaria, List.of(13L))
    );
    verify(repositorioVeterinariaMock, never()).guardar(any());
  }

  @Test
  public void actualizarPerfilDeberiaGuardarCambiosSinCambiarLaClave() throws Exception {
    Veterinaria existente = datosRegistroValidos();
    existente.setId(30L);
    existente.getUsuario().setId(40L);
    Veterinaria cambios = datosRegistroValidos();
    cambios.setNombre("Clínica nueva");
    cambios.setTelefono("4444-2222");
    cambios.setCuit("30-71234567-2");
    when(repositorioVeterinariaMock.buscarPorUsuario(40L)).thenReturn(existente);
    when(repositorioServicioMock.buscarPorId(13L)).thenReturn(cirugia);
    when(repositorioVeterinariaMock.buscarPorCuit("30712345672")).thenReturn(null);

    servicioVeterinaria.actualizarPerfil(40L, cambios, "nuevo@test.com", List.of(13L));

    assertThat(existente.getNombre(), equalTo("Clínica nueva"));
    assertThat(existente.getTelefono(), equalTo("4444-2222"));
    assertThat(existente.getUsuario().getEmail(), equalTo("nuevo@test.com"));
    assertThat(existente.getUsuario().getPassword(), equalTo("secreto"));
    assertThat(existente.getServicios(), contains(cirugia));
  }

  @Test
  public void actualizarPerfilConEmailDeOtraCuentaDeberiaRechazarlo() {
    Veterinaria existente = datosRegistroValidos();
    existente.setId(30L);
    existente.getUsuario().setId(40L);
    Veterinaria cambios = datosRegistroValidos();
    Usuario otraCuenta = new Usuario();
    otraCuenta.setId(99L);
    when(repositorioVeterinariaMock.buscarPorUsuario(40L)).thenReturn(existente);
    when(repositorioUsuarioMock.buscar("ocupado@test.com")).thenReturn(otraCuenta);

    assertThrows(
      UsuarioExistente.class,
      () -> servicioVeterinaria.actualizarPerfil(40L, cambios, "ocupado@test.com", List.of(13L))
    );
  }

  private Veterinaria datosRegistroValidos() {
    Veterinaria veterinaria = new Veterinaria();
    veterinaria.setNombre("Veterinaria Central");
    veterinaria.setCuit("30-71234567-1");
    veterinaria.setDireccion("Calle 123");
    veterinaria.setTelefono("4444-1111");
    veterinaria.setHoraApertura(LocalTime.of(9, 0));
    veterinaria.setHoraCierre(LocalTime.of(18, 0));
    veterinaria.setDiasAtencion(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));
    Usuario usuario = new Usuario();
    usuario.setEmail("vet@test.com");
    usuario.setPassword("secreto");
    veterinaria.setUsuario(usuario);
    return veterinaria;
  }
}
