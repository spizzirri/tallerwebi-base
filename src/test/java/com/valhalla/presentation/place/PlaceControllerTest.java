package com.valhalla.presentation.place;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;

import com.valhalla.domain.place.PlaceService;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.plan.PlanSettings;
import java.security.Principal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.servlet.ModelAndView;

public class PlaceControllerTest {

  private PlaceController controller;

  @BeforeEach
  public void init() {
    // [PSHARE] El controller gano PlaceService y PlanService para la ficha /places/{id}; el
    // listado de /explore no los toca, asi que mocks alcanzan para esta unit test.
    controller = new PlaceController(mock(PlaceService.class), mock(PlanService.class));
  }

  @Test
  public void shouldReturnTheExploreView() {
    ModelAndView result = controller.listPlaces(null);

    assertThat(result.getViewName(), is(equalTo("pages/places/list")));
  }

  /**
   * El listado de /explore lo arma el JS pidiendo /api/places: el catalogo en el modelo es una
   * lectura que ningun template consume.
   */
  @Test
  public void shouldNotPutTheCatalogueInTheModel() {
    ModelAndView result = controller.listPlaces(null);

    assertThat(result.getModel().get("places"), is(nullValue()));
    assertThat(result.getModel().get("categories"), is(nullValue()));
  }

  @Test
  public void shouldBuildTheCreatePlanPanelForTheView() {
    ModelAndView result = controller.listPlaces(null);

    assertThat(result.getModel().get("defaultPlanSettings"), is(instanceOf(PlanSettings.class)));
    assertThat(result.getModel().get("today"), is(equalTo(LocalDate.now().toString())));
    assertThat(result.getModel().get("isLogged"), is(equalTo(false)));
  }

  /** [APL/T9] La sesion es el gate del picker del panel de detalle: sin ella no se pinta. */
  @Test
  public void shouldMarkTheExploreViewAsLoggedForARealSession() {
    Principal user = new UsernamePasswordAuthenticationToken(
      "someone@unlam.edu.ar",
      "secret",
      AuthorityUtils.NO_AUTHORITIES
    );

    ModelAndView result = controller.listPlaces(user);

    assertThat(result.getModel().get("isLogged"), is(equalTo(true)));
  }

  /** [APL/T9] El token anonimo de Spring Security tambien cuenta como sin sesion. */
  @Test
  public void shouldTreatTheAnonymousTokenAsLoggedOut() {
    Authentication anonymous = new AnonymousAuthenticationToken(
      "key",
      "anonymousUser",
      AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
    );

    ModelAndView result = controller.listPlaces(anonymous);

    assertThat(result.getModel().get("isLogged"), is(equalTo(false)));
  }
}
