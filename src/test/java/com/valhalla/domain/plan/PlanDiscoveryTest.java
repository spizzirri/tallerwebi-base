package com.valhalla.domain.plan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceCategory;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.rating.RatingSummary;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Unit tests of {@link PlanDiscovery}: the score formula and the whole ordering chain. Pure Java, no
 * Spring and no database, so every tie-break is exercised with data built by hand.
 */
public class PlanDiscoveryTest {

  private static final double EPSILON = 0.005;
  private static final LocalDate TODAY = LocalDate.of(2026, 5, 10);

  @Test
  public void U01_popularityScore_sinCalificacionesDevuelveElPrior() {
    assertThat(PlanDiscovery.popularityScore(null, 0L), is(closeTo(3.7, EPSILON)));
  }

  @Test
  public void U02_popularityScore_cuatroVotosConPromedioCuatroDaTresComaOchentaYTres() {
    // (5 * 3.7 + 4.0 * 4) / (5 + 4) = 34.5 / 9 = 3.8333...
    assertThat(PlanDiscovery.popularityScore(4.0, 4L), is(closeTo(3.83, EPSILON)));
  }

  @Test
  public void U03_unSoloVotoDeCincoQuedaDebajoDeVeinteVotosDeCuatroComaCinco() {
    Plan singleVote = plan(1, "Un solo voto", null);
    Plan manyVotes = plan(2, "Muchos votos", null);
    Map<UUID, RatingSummary> summaries = Map.of(
      singleVote.getId(),
      new RatingSummary(singleVote.getId(), 5.0, 1L),
      manyVotes.getId(),
      new RatingSummary(manyVotes.getId(), 4.5, 20L)
    );

    // (5 * 3.7 + 5.0) / 6 = 3.92 contra (5 * 3.7 + 90) / 25 = 4.34
    assertThat(PlanDiscovery.popularityScore(5.0, 1L), is(closeTo(3.92, EPSILON)));
    assertThat(PlanDiscovery.popularityScore(4.5, 20L), is(closeTo(4.34, EPSILON)));

    List<Plan> ordered = PlanDiscovery.orderByPopularity(
      List.of(singleVote, manyVotes),
      summaries,
      Map.of()
    );

    assertThat(ordered.get(0).getId(), is(equalTo(manyVotes.getId())));
  }

  @Test
  public void U04_sinCalificacionesQuedaDespuesDeUnCalificadoAunqueTengaMasParticipantes() {
    Plan rated = plan(1, "Calificado", null);
    Plan unrated = plan(2, "Sin calificar", null);
    Map<UUID, RatingSummary> summaries = Map.of(
      rated.getId(),
      new RatingSummary(rated.getId(), 4.0, 4L)
    );

    List<Plan> ordered = PlanDiscovery.orderByPopularity(
      List.of(unrated, rated),
      summaries,
      Map.of(rated.getId(), 0L, unrated.getId(), 10L)
    );

    assertThat(ordered.get(0).getId(), is(equalTo(rated.getId())));
    assertThat(ordered.get(1).getId(), is(equalTo(unrated.getId())));
  }

  @Test
  public void U05_mismaPopularidadEntreCalificadosGanaElDeMasParticipantes() {
    Plan few = plan(1, "Pocos", null);
    Plan many = plan(2, "Muchos", null);
    Map<UUID, RatingSummary> summaries = Map.of(
      few.getId(),
      new RatingSummary(few.getId(), 4.0, 10L),
      many.getId(),
      new RatingSummary(many.getId(), 4.0, 10L)
    );

    List<Plan> ordered = PlanDiscovery.orderByPopularity(
      List.of(few, many),
      summaries,
      Map.of(few.getId(), 2L, many.getId(), 5L)
    );

    assertThat(ordered.get(0).getId(), is(equalTo(many.getId())));
  }

  @Test
  public void U06_mismoGrupoPopularidadYParticipantesGanaLaFechaMasProxima() {
    Plan later = plan(1, "Despues", LocalDate.of(2026, 3, 1));
    Plan sooner = plan(2, "Antes", LocalDate.of(2026, 1, 1));
    Map<UUID, RatingSummary> summaries = Map.of(
      later.getId(),
      new RatingSummary(later.getId(), 4.0, 10L),
      sooner.getId(),
      new RatingSummary(sooner.getId(), 4.0, 10L)
    );

    List<Plan> ordered = PlanDiscovery.orderByPopularity(
      List.of(later, sooner),
      summaries,
      Map.of(later.getId(), 3L, sooner.getId(), 3L)
    );

    assertThat(ordered.get(0).getId(), is(equalTo(sooner.getId())));
  }

  @Test
  public void U07_fechasNulasVanAlFinalDelDesempatePorFecha() {
    Plan withoutDate = plan(1, "Sin fecha", null);
    Plan withDate = plan(2, "Con fecha", LocalDate.of(2026, 6, 1));
    Map<UUID, RatingSummary> summaries = Map.of(
      withoutDate.getId(),
      new RatingSummary(withoutDate.getId(), 4.0, 10L),
      withDate.getId(),
      new RatingSummary(withDate.getId(), 4.0, 10L)
    );

    List<Plan> ordered = PlanDiscovery.orderByPopularity(
      List.of(withoutDate, withDate),
      summaries,
      Map.of(withoutDate.getId(), 3L, withDate.getId(), 3L)
    );

    assertThat(ordered.get(0).getId(), is(equalTo(withDate.getId())));
    assertThat(ordered.get(1).getId(), is(equalTo(withoutDate.getId())));
  }

  @Test
  public void U08_empateTotalDesempataPorNombreCaseInsensitiveYDespuesPorId() {
    Plan beta = plan(1, "Beta", LocalDate.of(2026, 6, 1));
    Plan alfa = plan(2, "alfa", LocalDate.of(2026, 6, 1));
    Map<UUID, RatingSummary> summaries = Map.of(
      beta.getId(),
      new RatingSummary(beta.getId(), 4.0, 10L),
      alfa.getId(),
      new RatingSummary(alfa.getId(), 4.0, 10L)
    );
    Map<UUID, Long> participants = Map.of(beta.getId(), 3L, alfa.getId(), 3L);

    List<Plan> ordered = PlanDiscovery.orderByPopularity(
      List.of(beta, alfa),
      summaries,
      participants
    );

    // "alfa" < "Beta" solo si la comparacion ignora mayusculas
    assertThat(ordered.get(0).getId(), is(equalTo(alfa.getId())));

    Plan sameNameLower = plan(2, "alfa", LocalDate.of(2026, 6, 1));
    Plan sameNameUpper = plan(1, "ALFA", LocalDate.of(2026, 6, 1));
    Map<UUID, RatingSummary> sameNameSummaries = Map.of(
      sameNameLower.getId(),
      new RatingSummary(sameNameLower.getId(), 4.0, 10L),
      sameNameUpper.getId(),
      new RatingSummary(sameNameUpper.getId(), 4.0, 10L)
    );

    List<Plan> byId = PlanDiscovery.orderByPopularity(
      List.of(sameNameLower, sameNameUpper),
      sameNameSummaries,
      Map.of(sameNameLower.getId(), 3L, sameNameUpper.getId(), 3L)
    );

    assertThat(byId.get(0).getId(), is(equalTo(sameNameUpper.getId())));
  }

  @Test
  public void U09_elMismoInputOrdenadoDosVecesProduceLaMismaLista() {
    Plan first = plan(1, "Primero", LocalDate.of(2026, 2, 1));
    Plan second = plan(2, "Segundo", LocalDate.of(2026, 1, 1));
    Map<UUID, RatingSummary> summaries = Map.of(
      first.getId(),
      new RatingSummary(first.getId(), 5.0, 2L)
    );
    Map<UUID, Long> participants = Map.of(first.getId(), 4L, second.getId(), 1L);

    List<Plan> once = PlanDiscovery.orderByPopularity(
      List.of(second, first),
      summaries,
      participants
    );
    List<Plan> twice = PlanDiscovery.orderByPopularity(
      List.of(second, first),
      summaries,
      participants
    );

    assertThat(twice, is(equalTo(once)));
  }

  @Test
  public void U10_listaVaciaDevuelveListaVaciaSinError() {
    List<Plan> ordered = PlanDiscovery.orderByPopularity(List.of(), Map.of(), Map.of());

    assertThat(ordered, is(empty()));
  }

  @Test
  public void U11_planSinParticipantesSeOrdenaConCeroYNoSeDescarta() {
    Plan withoutParticipants = plan(1, "Sin participantes", null);
    Plan withParticipants = plan(2, "Con participantes", null);
    Map<UUID, RatingSummary> summaries = Map.of(
      withoutParticipants.getId(),
      new RatingSummary(withoutParticipants.getId(), 4.0, 10L),
      withParticipants.getId(),
      new RatingSummary(withParticipants.getId(), 4.0, 10L)
    );

    List<Plan> ordered = PlanDiscovery.orderByPopularity(
      List.of(withoutParticipants, withParticipants),
      summaries,
      Map.of(withParticipants.getId(), 1L)
    );

    assertThat(ordered, hasSize(2));
    assertThat(ordered.get(0).getId(), is(equalTo(withParticipants.getId())));
    assertThat(ordered.get(1).getId(), is(equalTo(withoutParticipants.getId())));
  }

  @Test
  public void U12_orderByPopularityNoModificaLaListaOriginal() {
    Plan second = plan(1, "Segundo", LocalDate.of(2026, 2, 1));
    Plan first = plan(2, "Primero", LocalDate.of(2026, 1, 1));
    List<Plan> original = new ArrayList<>(List.of(second, first));
    Map<UUID, RatingSummary> summaries = Map.of(
      first.getId(),
      new RatingSummary(first.getId(), 5.0, 3L)
    );

    List<Plan> ordered = PlanDiscovery.orderByPopularity(original, summaries, Map.of());

    assertThat(ordered, is(not(sameInstance(original))));
    assertThat(original.get(0).getId(), is(equalTo(second.getId())));
    assertThat(ordered.get(0).getId(), is(equalTo(first.getId())));
  }

  @Test
  public void popularityScoreWithNullCountStillUsesThePrior() {
    assertThat(PlanDiscovery.popularityScore(4.0, null), is(closeTo(3.7, EPSILON)));
  }

  @Test
  public void popularityTreatsNullAndZeroVoteSummariesAsUnrated() {
    Plan rated = plan(201, "Calificado", null);
    Plan nullCount = plan(202, "Conteo nulo", null);
    Plan zeroCount = plan(203, "Conteo cero", null);
    Map<UUID, RatingSummary> summaries = Map.of(
      rated.getId(),
      new RatingSummary(rated.getId(), 4.0, 4L),
      nullCount.getId(),
      new RatingSummary(nullCount.getId(), 4.0, null),
      zeroCount.getId(),
      new RatingSummary(zeroCount.getId(), 4.0, 0L)
    );

    List<Plan> ordered = PlanDiscovery.orderByPopularity(
      List.of(nullCount, zeroCount, rated),
      summaries,
      Map.of()
    );

    assertThat(ordered, hasSize(3));
    assertThat(ordered.get(0).getId(), is(equalTo(rated.getId())));
  }

  @Test
  public void searchMatchesTextByNameIgnoringCase() {
    Plan museum = plan(101, "Museo de Arte", TODAY);
    Plan bar = plan(102, "Bar Central", TODAY);

    List<Plan> found = discover(
      List.of(museum, bar),
      PlanDiscoveryQuery.parse("MUSEO", null, null, null)
    );

    assertThat(found, hasSize(1));
    assertThat(found.get(0).getId(), is(equalTo(museum.getId())));
  }

  @Test
  public void searchMatchesTextByDescription() {
    Plan plan = plan(103, "Salida del sabado", "Recorrido por el museo", TODAY);
    Plan other = plan(104, "Cena", "Pizzeria del barrio", TODAY);

    List<Plan> found = discover(
      List.of(plan, other),
      PlanDiscoveryQuery.parse("museo", null, null, null)
    );

    assertThat(found, hasSize(1));
    assertThat(found.get(0).getId(), is(equalTo(plan.getId())));
  }

  @Test
  public void searchWithoutMatchesReturnsEmptyList() {
    List<Plan> found = discover(
      List.of(plan(105, "Museo", TODAY)),
      PlanDiscoveryQuery.parse("zzz", null, null, null)
    );

    assertThat(found, is(empty()));
  }

  @Test
  public void searchBlankTextDoesNotFilter() {
    Plan first = plan(106, "Primero", TODAY);
    Plan second = plan(107, "Segundo", TODAY);

    List<Plan> found = discover(
      List.of(first, second),
      PlanDiscoveryQuery.parse("   ", null, null, null)
    );

    assertThat(found, hasSize(2));
  }

  @Test
  public void searchWithNullDescriptionDoesNotBreak() {
    Plan withoutDescription = plan(108, "Museo", TODAY);

    List<Plan> found = discover(
      List.of(withoutDescription),
      PlanDiscoveryQuery.parse("museo", null, null, null)
    );
    List<Plan> notFound = discover(
      List.of(withoutDescription),
      PlanDiscoveryQuery.parse("zzz", null, null, null)
    );

    assertThat(found, hasSize(1));
    assertThat(notFound, is(empty()));
  }

  @Test
  public void searchByCategoryReturnsOnlyPlansWithThatPlace() {
    Plan bar = plan(109, "Noche de bares", TODAY);
    addPlace(bar, PlaceCategory.BAR);
    addPlace(bar, PlaceCategory.CAFE);
    Plan park = plan(110, "Tarde de parque", TODAY);
    addPlace(park, PlaceCategory.PARK);

    List<Plan> found = discover(
      List.of(bar, park),
      PlanDiscoveryQuery.parse(null, "BAR", null, null)
    );

    assertThat(found, hasSize(1));
    assertThat(found.get(0).getId(), is(equalTo(bar.getId())));
  }

  @Test
  public void searchByCategoryExcludesPlanWithoutPlaces() {
    Plan noPlaces = plan(111, "Sin lugares", TODAY);
    Plan bar = plan(112, "Con bar", TODAY);
    addPlace(bar, PlaceCategory.BAR);

    List<Plan> found = discover(
      List.of(noPlaces, bar),
      PlanDiscoveryQuery.parse(null, "BAR", null, null)
    );

    assertThat(found, hasSize(1));
    assertThat(found.get(0).getId(), is(equalTo(bar.getId())));
  }

  @Test
  public void searchByCategoryIgnoresEntriesWithoutPlace() {
    Plan orphan = plan(204, "Entrada huerfana", TODAY);
    orphan.addPlanPlace(new PlanPlace());
    Plan bar = plan(205, "Con bar", TODAY);
    addPlace(bar, PlaceCategory.BAR);

    List<Plan> found = discover(
      List.of(orphan, bar),
      PlanDiscoveryQuery.parse(null, "BAR", null, null)
    );

    assertThat(found, hasSize(1));
    assertThat(found.get(0).getId(), is(equalTo(bar.getId())));
  }

  @Test
  public void searchUpcomingIncludesTodayAndExcludesPast() {
    Plan today = plan(113, "Hoy", TODAY);
    Plan yesterday = plan(114, "Ayer", TODAY.minusDays(1));
    Plan tomorrow = plan(115, "Manana", TODAY.plusDays(1));

    List<Plan> found = discover(
      List.of(today, yesterday, tomorrow),
      PlanDiscoveryQuery.parse(null, null, "upcoming", null)
    );

    assertThat(found, hasSize(2));
    assertThat(found.get(0).getId(), is(equalTo(today.getId())));
    assertThat(found.get(1).getId(), is(equalTo(tomorrow.getId())));
  }

  @Test
  public void searchPastIncludesOnlyDatesBeforeToday() {
    Plan today = plan(116, "Hoy", TODAY);
    Plan yesterday = plan(117, "Ayer", TODAY.minusDays(1));

    List<Plan> found = discover(
      List.of(today, yesterday),
      PlanDiscoveryQuery.parse(null, null, "past", null)
    );

    assertThat(found, hasSize(1));
    assertThat(found.get(0).getId(), is(equalTo(yesterday.getId())));
  }

  @Test
  public void searchDateWindowExcludesPlansWithoutDate() {
    Plan withDate = plan(118, "Con fecha", TODAY);
    Plan withoutDate = plan(119, "Sin fecha", null);

    List<Plan> upcoming = discover(
      List.of(withDate, withoutDate),
      PlanDiscoveryQuery.parse(null, null, "upcoming", null)
    );
    List<Plan> past = discover(
      List.of(withDate, withoutDate),
      PlanDiscoveryQuery.parse(null, null, "past", null)
    );
    List<Plan> all = discover(
      List.of(withDate, withoutDate),
      PlanDiscoveryQuery.parse(null, null, "all", null)
    );

    assertThat(upcoming, hasSize(1));
    assertThat(past, is(empty()));
    assertThat(all, hasSize(2));
  }

  @Test
  public void searchCombinesTextCategoryAndDateWithAnd() {
    Plan match = plan(120, "Noche de museo", "Museo y bar", TODAY.plusDays(1));
    addPlace(match, PlaceCategory.BAR);
    Plan wrongCategory = plan(121, "Museo sin bar", "Museo", TODAY.plusDays(1));
    addPlace(wrongCategory, PlaceCategory.MUSEUM);
    Plan wrongDate = plan(122, "Museo viejo", "Museo y bar", TODAY.minusDays(1));
    addPlace(wrongDate, PlaceCategory.BAR);

    List<Plan> found = discover(
      List.of(match, wrongCategory, wrongDate),
      PlanDiscoveryQuery.parse("museo", "BAR", "upcoming", null)
    );

    assertThat(found, hasSize(1));
    assertThat(found.get(0).getId(), is(equalTo(match.getId())));
  }

  @Test
  public void searchSortByDateOrdersAscendingWithNullsLast() {
    Plan later = plan(123, "Despues", LocalDate.of(2026, 3, 1));
    Plan sooner = plan(124, "Antes", LocalDate.of(2026, 1, 1));
    Plan withoutDate = plan(125, "Sin fecha", null);

    List<Plan> found = discover(
      List.of(later, withoutDate, sooner),
      PlanDiscoveryQuery.parse(null, null, null, "date")
    );

    assertThat(found.get(0).getId(), is(equalTo(sooner.getId())));
    assertThat(found.get(1).getId(), is(equalTo(later.getId())));
    assertThat(found.get(2).getId(), is(equalTo(withoutDate.getId())));
  }

  @Test
  public void searchSortByNameOrdersCaseInsensitive() {
    Plan beta = plan(126, "beta", TODAY);
    Plan alfa = plan(127, "Alfa", TODAY);
    Plan gama = plan(128, "gama", TODAY);

    List<Plan> found = discover(
      List.of(beta, gama, alfa),
      PlanDiscoveryQuery.parse(null, null, null, "name")
    );

    assertThat(found.get(0).getId(), is(equalTo(alfa.getId())));
    assertThat(found.get(1).getId(), is(equalTo(beta.getId())));
    assertThat(found.get(2).getId(), is(equalTo(gama.getId())));
  }

  @Test
  public void searchSortByDateBreaksTiesByName() {
    Plan zeta = plan(206, "Zeta", TODAY);
    Plan alfa = plan(207, "Alfa", TODAY);

    List<Plan> found = discover(
      List.of(zeta, alfa),
      PlanDiscoveryQuery.parse(null, null, null, "date")
    );

    assertThat(found.get(0).getId(), is(equalTo(alfa.getId())));
    assertThat(found.get(1).getId(), is(equalTo(zeta.getId())));
  }

  @Test
  public void searchSortByNameBreaksTiesByDate() {
    Plan later = plan(208, "Museo", TODAY.plusDays(1));
    Plan sooner = plan(209, "Museo", TODAY);

    List<Plan> found = discover(
      List.of(later, sooner),
      PlanDiscoveryQuery.parse(null, null, null, "name")
    );

    assertThat(found.get(0).getId(), is(equalTo(sooner.getId())));
    assertThat(found.get(1).getId(), is(equalTo(later.getId())));
  }

  private List<Plan> discover(List<Plan> plans, PlanDiscoveryQuery query) {
    return PlanDiscovery.discover(plans, Map.of(), Map.of(), query, TODAY);
  }

  private Plan plan(int number, String name, LocalDate eventDate) {
    return plan(number, name, null, eventDate);
  }

  private Plan plan(int number, String name, String description, LocalDate eventDate) {
    Plan plan = new Plan();
    plan.setId(UUID.fromString(String.format("0192f0a1-0000-7000-8000-%012d", number)));
    plan.setName(name);
    plan.setDescription(description);
    plan.setEventDate(eventDate);
    return plan;
  }

  private void addPlace(Plan plan, PlaceCategory category) {
    Place place = new Place();
    place.setCategory(category);
    PlanPlace planPlace = new PlanPlace();
    planPlace.setPlace(place);
    plan.addPlanPlace(planPlace);
  }
}
