package com.tallerwebi.dominio.HijoTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.AliasDeRetiro.ServicioGeneradorAlias;
import com.tallerwebi.dominio.Hijos.Hijo;
import com.tallerwebi.dominio.Hijos.RepositorioHijo;
import com.tallerwebi.dominio.Hijos.ServicioHijo;
import com.tallerwebi.dominio.Hijos.ServicioHijoImpl;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.dominio.excepcion.HijoExistenteException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ServicioHijoTest {

  private Usuario usuarioMock;
  private ServicioHijo servicioHijo;
  private RepositorioHijo repositorioHijoMock;
  private ServicioGeneradorAlias servicioGeneradorAliasMock;
  private Hijo hijoMock;

  @BeforeEach
  public void init() {
    repositorioHijoMock = Mockito.mock(RepositorioHijo.class);
    servicioGeneradorAliasMock = Mockito.mock(ServicioGeneradorAlias.class);

    usuarioMock = Mockito.mock(Usuario.class);
    servicioHijo = new ServicioHijoImpl(repositorioHijoMock, servicioGeneradorAliasMock);
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

    servicioHijo.guardarHijo(hijoMock, usuarioMock);

    verify(repositorioHijoMock, times(1)).guardar(hijoMock);
  }

  @Test
  public void guardarHijoSiYaExisteDeberiaLanzarExcepcion() {
    when(hijoMock.getDni()).thenReturn(12345678L);
    when(repositorioHijoMock.existeHijoPorDni(12345678L)).thenReturn(true);

    assertThrows(
      HijoExistenteException.class,
      () -> servicioHijo.guardarHijo(hijoMock, usuarioMock)
    );
    verify(repositorioHijoMock, times(0)).guardar(hijoMock);
  }
}
