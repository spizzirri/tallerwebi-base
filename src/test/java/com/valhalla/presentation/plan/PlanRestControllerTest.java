package com.valhalla.presentation.plan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceCategory;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.user.User;
import com.valhalla.presentation.plan.PlanFeedCards.Card;
import com.valhalla.presentation.plan.PlanRestController.PlanSummary;
import java.lang.reflect.RecordComponent;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * Unit tests of {@link PlanRestController}. The service is mocked, so what is under test is the
 * mapping of {@code GET /api/plans} (its two branches on {@code Principal}, the endpoint the
 * "Add to plan" modal calls) and of {@code GET /api/plans/public} (the feed the sidebar of
 * /explore consumes: visibility, own-plan exclusion, ordering, window and payload shape).
 */
public class PlanRestControllerTest {

  private static final String OWNER_EMAIL = "dueno@test.com";
  private static final UUID PLAN_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000007");

  private PlanRestController controller;
  private PlanService planServiceMock;

  @BeforeEach
  public void init() {
    planServiceMock = mock(PlanService.class);
    controller = new PlanRestController(planServiceMock);
  }

  @Test
  public void shouldReturnAnEmptyListWithoutAskingTheServiceWhenThereIsNoPrincipal() {
    List<PlanSummary> plans = controller.getUserPlans(null);

    assertThat(plans, is(empty()));
    verifyNoInteractions(planServiceMock);
  }

  @Test
  public void shouldMapEveryPlanOfTheAuthenticatedUser() {
    Plan plan = planWithOnePlace();
    when(planServiceMock.getPlansByUserEmail(OWNER_EMAIL)).thenReturn(List.of(plan));

    List<PlanSummary> plans = controller.getUserPlans(principal());

    assertThat(plans, hasSize(1));
    PlanSummary summary = plans.get(0);
    assertThat(summary.id(), is(PLAN_ID));
    assertThat(summary.name(), is("Viaje a Bariloche"));
    assertThat(summary.eventDate(), is("2026-12-31"));
    assertThat(summary.isPublic(), is(true));
    assertThat(summary.planPlaces(), hasSize(1));
    assertThat(summary.planPlaces().get(0).id(), is(11L));
    assertThat(summary.planPlaces().get(0).placeId(), is(4L));
    assertThat(summary.planPlaces().get(0).name(), is("MALBA"));
    assertThat(summary.planPlaces().get(0).category(), is("MUSEUM"));
    assertThat(summary.planPlaces().get(0).latitude(), is(-34.6098));
    assertThat(summary.planPlaces().get(0).longitude(), is(-58.3711));
    verify(planServiceMock).getPlansByUserEmail(OWNER_EMAIL);
  }

  @Test
  public void shouldLeaveTheEventDateNullWhenThePlanHasNone() {
    Plan plan = new Plan();
    plan.setId(PLAN_ID);
    plan.setName("Sin fecha");
    when(planServiceMock.getPlansByUserEmail(OWNER_EMAIL)).thenReturn(List.of(plan));

    List<PlanSummary> plans = controller.getUserPlans(principal());

    assertThat(plans, hasSize(1));
    assertThat(plans.get(0).eventDate(), is(nullValue()));
    assertThat(plans.get(0).planPlaces(), is(empty()));
  }

  // --- GET /api/plans/public ---

  @Test
  public void shouldReturnOnlyPublicPlansFromThePublicFeed() {
    Plan publicPlan = feedPlan("Publico", LocalDate.of(2026, 10, 18), true, "ajeno@test.com");
    Plan privatePlan = feedPlan("Privado", LocalDate.of(2026, 10, 19), false, "ajeno@test.com");
    when(planServiceMock.getPublicPlans()).thenReturn(List.of(publicPlan, privatePlan));

    List<Card> cards = controller.getPublicPlans(null);

    assertThat(cards, hasSize(1));
    assertThat(cards.get(0).name(), is("Publico"));
  }

  @Test
  public void shouldExcludeTheCallersOwnPlansWhenAPrincipalIsPresent() {
    Plan ownPlan = feedPlan("Mi plan", LocalDate.of(2026, 10, 18), true, OWNER_EMAIL);
    Plan otherPlan = feedPlan("Ajeno", LocalDate.of(2026, 10, 19), true, "otro@test.com");
    when(planServiceMock.getPublicPlans()).thenReturn(List.of(ownPlan, otherPlan));

    List<Card> cards = controller.getPublicPlans(principal());

    assertThat(cards, hasSize(1));
    assertThat(cards.get(0).name(), is("Ajeno"));
  }

  @Test
  public void shouldNotFilterOwnPlansForAnonymousCallers() {
    // sin principal no hay identidad: el anónimo ve todos los planes públicos, propios o no
    Plan somePlan = feedPlan("Plan de cualquiera", LocalDate.of(2026, 10, 18), true, OWNER_EMAIL);
    when(planServiceMock.getPublicPlans()).thenReturn(List.of(somePlan));

    List<Card> cards = controller.getPublicPlans(null);

    assertThat(cards, hasSize(1));
  }

  @Test
  public void shouldOrderByEventDateAscendingWithUndatedPlansLast() {
    Plan undated = feedPlan("Sin fecha", null, true, "ajeno@test.com");
    Plan late = feedPlan("Tarde", LocalDate.of(2026, 12, 31), true, "ajeno@test.com");
    Plan early = feedPlan("Pronto", LocalDate.of(2026, 10, 18), true, "ajeno@test.com");
    when(planServiceMock.getPublicPlans()).thenReturn(List.of(undated, late, early));

    List<Card> cards = controller.getPublicPlans(null);

    assertThat(
      cards.stream().map(Card::name).toList(),
      is(equalTo(List.of("Pronto", "Tarde", "Sin fecha")))
    );
  }

  @Test
  public void shouldReturnAtMostFivePlans() {
    List<Plan> seven = new ArrayList<>();
    for (int day = 1; day <= 7; day++) {
      seven.add(feedPlan("Plan " + day, LocalDate.of(2026, 10, day), true, "ajeno@test.com"));
    }
    when(planServiceMock.getPublicPlans()).thenReturn(seven);

    List<Card> cards = controller.getPublicPlans(null);

    assertThat(cards, hasSize(5));
    assertThat(cards.get(0).name(), is("Plan 1"));
    assertThat(cards.get(4).name(), is("Plan 5"));
  }

  @Test
  public void shouldNotExposeShortCodeNorEmailsInThePublicPayload() throws Exception {
    Plan plan = feedPlan("Plan con secretos", LocalDate.of(2026, 12, 31), true, "ana@privado.com");
    plan.setShortCode("SECRET01");
    plan.getAdministrator().setFirstName("Ana");
    plan.getAdministrator().setLastName("Perez");
    User participant = new User();
    participant.setEmail("invitado@privado.com");
    plan.getParticipants().add(participant);
    when(planServiceMock.getPublicPlans()).thenReturn(List.of(plan));

    String json = new ObjectMapper().writeValueAsString(controller.getPublicPlans(null));

    assertThat(json, not(containsString("SECRET01")));
    assertThat(json, not(containsString("shortCode")));
    assertThat(json, not(containsString("planPlaces")));
    assertThat(json, not(containsString("ana@privado.com")));
    assertThat(json, not(containsString("invitado@privado.com")));
    assertThat(json, containsString("administratorName"));
    // los componentes del record son exactamente las claves del JSON de respuesta
    assertThat(
      Arrays.stream(Card.class.getRecordComponents()).map(RecordComponent::getName).toList(),
      is(
        equalTo(
          List.of(
            "id",
            "name",
            "description",
            "eventDate",
            "placeCount",
            "administratorName",
            "cover"
          )
        )
      )
    );
  }

  // --- helpers ---

  private static Plan feedPlan(
    String name,
    LocalDate eventDate,
    boolean isPublic,
    String adminEmail
  ) {
    Plan plan = new Plan();
    plan.setId(UUID.randomUUID());
    plan.setName(name);
    plan.setEventDate(eventDate);
    plan.setIsPublic(isPublic);
    User administrator = new User();
    administrator.setEmail(adminEmail);
    plan.setAdministrator(administrator);
    return plan;
  }

  private static Principal principal() {
    return new UsernamePasswordAuthenticationToken(OWNER_EMAIL, null);
  }

  private static Plan planWithOnePlace() {
    Place place = new Place();
    place.setId(4L);
    place.setName("MALBA");
    place.setCategory(PlaceCategory.MUSEUM);
    place.setLatitude(-34.6098);
    place.setLongitude(-58.3711);

    PlanPlace planPlace = new PlanPlace();
    planPlace.setId(11L);
    planPlace.setPlace(place);

    Plan plan = new Plan();
    plan.setId(PLAN_ID);
    plan.setName("Viaje a Bariloche");
    plan.setEventDate(LocalDate.of(2026, 12, 31));
    plan.setIsPublic(true);
    plan.getPlanPlaces().add(planPlace);
    return plan;
  }
}
