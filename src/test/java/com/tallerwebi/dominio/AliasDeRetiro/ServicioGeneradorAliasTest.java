package com.tallerwebi.dominio.AliasDeRetiro;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Hijos.Hijo;
import com.tallerwebi.dominio.Hijos.RepositorioHijo;
import com.tallerwebi.dominio.Hijos.ServicioHijoImpl;
import com.tallerwebi.dominio.Usuario.Usuario;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ServicioGeneradorAliasTest {

  private ServicioGeneradorAlias servicioGeneradorAlias;
  private ServicioPalabrasAlias servicioPalabrasAliasMock;
  private RepositorioHijo repositorioHijoMock;

  @BeforeEach
  public void init() {
    servicioPalabrasAliasMock = Mockito.mock(ServicioPalabrasAlias.class);
    repositorioHijoMock = Mockito.mock(RepositorioHijo.class);

    when(servicioPalabrasAliasMock.obtenerColores())
      .thenReturn(List.of("ROJO", "AZUL", "VERDE", "AMARILLO", "VIOLETA"));

    when(servicioPalabrasAliasMock.obtenerAnimales())
      .thenReturn(List.of("GATO", "PERRO", "CONEJO", "TIGRE", "PANDA"));

    when(servicioPalabrasAliasMock.obtenerObjetos())
      .thenReturn(List.of("COMETA", "LAPIZ", "PELOTA", "BICICLETA", "TREN"));

    servicioGeneradorAlias =
      new ServicioGeneradorAliasImpl(servicioPalabrasAliasMock, repositorioHijoMock);
  }

  @Test
  void deberiaGenerarTodasLasCombinacionesPosibles() {
    List<String> aliases = servicioGeneradorAlias.obtenerTodasLasCombinaciones();

    assertEquals(125, aliases.size());
  }

  @Test
  void deberiaGenerarUnAliasDisponible() {
    when(repositorioHijoMock.existeAlias(anyString())).thenReturn(false);

    String alias = servicioGeneradorAlias.generarAliasDisponible();

    assertNotNull(alias);
  }

  @Test
  void deberiaRetornarNullCuandoNoHayAliasesDisponibles() {
    when(repositorioHijoMock.existeAlias(anyString())).thenReturn(true);

    String alias = servicioGeneradorAlias.generarAliasDisponible();

    assertNull(alias);
  }
}
