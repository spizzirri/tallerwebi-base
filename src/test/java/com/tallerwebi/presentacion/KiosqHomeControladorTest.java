package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.mockito.Mockito.when;

import com.tallerwebi.presentacion.Kiosquero.KiosqHomeControlador;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.ModelAndView;

public class KiosqHomeControladorTest {

  private KiosqHomeControlador kiosControlador;
  private HttpSession sessionMock;

  @BeforeEach
  public void init() {
    kiosControlador = new KiosqHomeControlador();
    sessionMock = Mockito.mock(HttpSession.class);
  }

  @Test
  public void siNoHayKiosqueroLogueadoDebeVolverAlLogin() {
    when(sessionMock.getAttribute("KIOSQUERO")).thenReturn(null);

    ModelAndView mav = kiosControlador.irAlHomeKiosquero(sessionMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/login"));
  }
}
