package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioVeterinariaTest {

  private ServicioVeterinaria servicioVeterinaria;
  private RepositorioServicio repositorioServicioMock;
  private Veterinaria conCirugia;
  private Veterinaria sinCirugia;
  private Servicio cirugia;

  @BeforeEach
  public void init() {
    RepositorioVeterinaria repositorioVeterinariaMock = mock(RepositorioVeterinaria.class);
    repositorioServicioMock = mock(RepositorioServicio.class);
    servicioVeterinaria =
      new ServicioVeterinariaImpl(repositorioVeterinariaMock, repositorioServicioMock);

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
}
