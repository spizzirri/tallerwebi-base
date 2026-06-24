package com.tallerwebi.dominio.HijoTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.AliasDeRetiro.ServicioGeneradorAlias;
import com.tallerwebi.dominio.Hijos.Hijo;
import com.tallerwebi.dominio.Hijos.RepositorioHijo;
import com.tallerwebi.dominio.Hijos.ServicioHijo;
import com.tallerwebi.dominio.Hijos.ServicioHijoImpl;
import com.tallerwebi.dominio.SubidaDeImgs.ServicioImagenes;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.dominio.excepcion.HijoExistenteException;
import com.tallerwebi.dominio.excepcion.HijoNoEncontradoException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

public class ServicioHijoTest {

  private Usuario usuarioMock;
  private ServicioHijo servicioHijo;
  private RepositorioHijo repositorioHijoMock;
  private ServicioGeneradorAlias servicioGeneradorAliasMock;
  private ServicioImagenes servicioImagenesMock;
  private Hijo hijoMock;

  @BeforeEach
  public void init() {
    repositorioHijoMock = Mockito.mock(RepositorioHijo.class);
    servicioGeneradorAliasMock = Mockito.mock(ServicioGeneradorAlias.class);

    servicioImagenesMock = Mockito.mock(ServicioImagenes.class);
    usuarioMock = Mockito.mock(Usuario.class);
    servicioHijo =
      new ServicioHijoImpl(repositorioHijoMock, servicioGeneradorAliasMock, servicioImagenesMock);
    hijoMock = Mockito.mock(Hijo.class);
  }

  @Test
  public void cuandoSeSoliciteHijosDebeBuscarEnElRepoYDevolverList() {
    List<Hijo> listaHijoSimulada = List.of(hijoMock);
    when(repositorioHijoMock.listarHijos(usuarioMock.getId())).thenReturn(listaHijoSimulada);

    List<Hijo> listaHijoObtenida = servicioHijo.obtenerHijosPorUsuario(usuarioMock.getId());
    assertThat(listaHijoObtenida, hasSize(1));
  }

  @Test
  public void guardarHijoSiNoExisteDeberiaGuardarlo() {
    when(hijoMock.getDni()).thenReturn(12345678L);
    when(repositorioHijoMock.existeHijoPorDni(12345678L)).thenReturn(false);

    servicioHijo.guardarHijo(hijoMock, null, usuarioMock);

    verify(repositorioHijoMock, times(1)).guardar(hijoMock);
  }

  @Test
  public void guardarHijoSiYaExisteDeberiaLanzarExcepcion() {
    when(hijoMock.getDni()).thenReturn(12345678L);
    when(repositorioHijoMock.existeHijoPorDni(12345678L)).thenReturn(true);

    assertThrows(
      HijoExistenteException.class,
      () -> servicioHijo.guardarHijo(hijoMock, null, usuarioMock)
    );
    verify(repositorioHijoMock, times(0)).guardar(hijoMock);
  }

  @Test
  public void guardarHijoConFotoDeberiaSubirLaImagenYGuardar() {
    // Creamos el mock de la foto
    MultipartFile fotoMock = mock(MultipartFile.class);
    when(fotoMock.isEmpty()).thenReturn(false); // Decimos que NO está vacía

    when(hijoMock.getDni()).thenReturn(12345678L);
    when(repositorioHijoMock.existeHijoPorDni(12345678L)).thenReturn(false);
    when(servicioImagenesMock.subirImagenHijo(any(), any())).thenReturn("url_de_la_foto");

    // Pasamos el fotoMock
    servicioHijo.guardarHijo(hijoMock, fotoMock, usuarioMock);

    // Verificamos que se setee la foto y se guarde
    verify(hijoMock, times(1)).setFotoPerfil("url_de_la_foto");
    verify(repositorioHijoMock, times(1)).guardar(hijoMock);
  }

  @Test
  public void editarHijoSiExisteYEsDelUsuarioDebeActualizarloYGuardarlo() {
    Long idHijo = 1L;
    Hijo hijoExistenteMock = Mockito.mock(Hijo.class);
    when(hijoExistenteMock.getPadre()).thenReturn(usuarioMock);
    when(usuarioMock.getId()).thenReturn(10L);

    when(repositorioHijoMock.buscarPorId(idHijo)).thenReturn(hijoExistenteMock);

    Hijo datosNuevosMock = Mockito.mock(Hijo.class);
    when(datosNuevosMock.getNombre()).thenReturn("Santiago");
    when(datosNuevosMock.getApellido()).thenReturn("Perez");

    servicioHijo.editarHijo(idHijo, datosNuevosMock, null, usuarioMock);

    verify(hijoExistenteMock, times(1)).setNombre("Santiago");
    verify(hijoExistenteMock, times(1)).setApellido("Perez");
    verify(repositorioHijoMock, times(1)).modificar(hijoExistenteMock);
  }

  @Test
  public void editarHijoSiNoExisteDebeLanzarExcepcion() {
    Long idHijo = 1L;
    when(repositorioHijoMock.buscarPorId(idHijo)).thenReturn(null);

    Hijo datosNuevosMock = Mockito.mock(Hijo.class);

    assertThrows(
      HijoNoEncontradoException.class,
      () -> servicioHijo.editarHijo(idHijo, datosNuevosMock, null, usuarioMock)
    );

    verify(repositorioHijoMock, times(0)).modificar(any(Hijo.class));
  }

  @Test
  public void editarHijoSiNoEsDelPadreDebeLanzarException() {
    Long idHijo = 1L;
    Usuario otroUsuarioMock = Mockito.mock(Usuario.class); // es otro el usuario que intenta modificar
    when(otroUsuarioMock.getId()).thenReturn(9L);

    Hijo hijoExistenteMock = Mockito.mock(Hijo.class);
    when(hijoExistenteMock.getPadre()).thenReturn(otroUsuarioMock);
    when(repositorioHijoMock.buscarPorId(idHijo)).thenReturn(hijoExistenteMock);

    when(usuarioMock.getId()).thenReturn(10L);
    Hijo datosNuevosMock = Mockito.mock(Hijo.class);

    assertThrows(
      HijoNoEncontradoException.class,
      () -> servicioHijo.editarHijo(idHijo, datosNuevosMock, null, usuarioMock)
    );

    verify(repositorioHijoMock, times(0)).modificar(any(Hijo.class));
  }

  @Test
  public void editarHijoConFotoValidaDebeSubirlaACloudinaryYSetearLaUrl() {
    // Given
    Long idHijo = 1L;
    Hijo hijoExistente = new Hijo(); // Objeto real para comprobar el cambio de estado
    hijoExistente.setPadre(usuarioMock);
    when(usuarioMock.getId()).thenReturn(10L);
    when(repositorioHijoMock.buscarPorId(idHijo)).thenReturn(hijoExistente);

    Hijo datosNuevosMock = Mockito.mock(Hijo.class);

    // Simulamos el archivo que viene del frontend
    MultipartFile fotoMock = Mockito.mock(MultipartFile.class);
    when(fotoMock.isEmpty()).thenReturn(false); // SÍ viene una foto válida

    // Imitamos la subida apuntando a la carpeta de hijos y el método exclusivo con IA
    when(servicioImagenesMock.subirImagenHijo(fotoMock, "KionetTWI/img_hijos"))
      .thenReturn("https://res.cloudinary.com/test/img_hijos/foto_santi.jpg");
    // When
    servicioHijo.editarHijo(idHijo, datosNuevosMock, fotoMock, usuarioMock);
    // Then
    verify(repositorioHijoMock, times(1)).modificar(hijoExistente);
    verify(servicioImagenesMock, times(1)).subirImagenHijo(fotoMock, "KionetTWI/img_hijos");

    assertThat(
      hijoExistente.getFotoPerfil(),
      equalTo("https://res.cloudinary.com/test/img_hijos/foto_santi.jpg")
    );
  }
}
