package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ResumenMascotasParaAsistenteTest {

  @Test
  public void DebeIndicarQueElUsuarioNoTieneMascotas() {
    Socio socio = new Socio();
    String texto = ResumenMascotasParaAsistente.formatear(socio);

    assertEquals("El socio no tiene mascotas registradas.", texto);
  }

  @Test
  public void conMascotaDeberiaIncluirNombreYEspecie() {
    Socio socio = new Socio();
    Mascota luna = new Mascota();
    luna.setId(1L);
    luna.setNombre("Luna");
    luna.setEspecie(Especie.PERRO);
    socio.getMascotas().add(luna);
    String texto = ResumenMascotasParaAsistente.formatear(socio);

    assertThat(texto, containsString("Luna"));
    assertThat(texto, containsString("PERRO"));
    assertThat(texto, containsString("id=1"));
  }
}
