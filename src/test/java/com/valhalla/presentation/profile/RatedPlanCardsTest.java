package com.valhalla.presentation.profile;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.valhalla.domain.rating.RatedPlanView;
import com.valhalla.domain.rating.RatingSummary;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Unit tests of {@link RatedPlanCards}: la fila de "Mis calificaciones" lleva el puntaje propio, el
 * agregado global redondeado y el badge solo cuando el plan dejo de ser accesible (AC-05/AC-06).
 */
public class RatedPlanCardsTest {

  private static final UUID PLAN_ID = UUID.fromString("0192f0a1-0000-7000-8000-0000000000ca");

  @Test
  public void anAccessiblePlanHasNoBadgeAndKeepsTheOwnScore() {
    Map<String, Object> row = RatedPlanCards.row(
      new RatedPlanView(PLAN_ID, "Bariloche", 4, true, false),
      new RatingSummary(PLAN_ID, 4.25, 8L)
    );

    assertThat(row.get("planId"), is(equalTo(PLAN_ID)));
    assertThat(row.get("name"), is(equalTo("Bariloche")));
    assertThat(row.get("score"), is(equalTo(4)));
    assertThat(row.get("accessible"), is(true));
    assertThat(row.get("member"), is(false));
    assertThat(row.get("badge"), is(nullValue()));
  }

  @Test
  public void theGlobalAggregateIsRoundedToOneDecimal() {
    Map<String, Object> row = RatedPlanCards.row(
      new RatedPlanView(PLAN_ID, "Bariloche", 3, true, true),
      new RatingSummary(PLAN_ID, 4.25, 8L)
    );

    assertThat(row.get("ratingAverage"), is(equalTo(4.3)));
    assertThat(row.get("ratingCount"), is(equalTo(8L)));
  }

  @Test
  public void anInaccessiblePlanCarriesTheUnavailableBadgeAndThePersistedName() {
    Map<String, Object> row = RatedPlanCards.row(
      new RatedPlanView(PLAN_ID, "Bariloche", 2, false, false),
      new RatingSummary(PLAN_ID, null, 0L)
    );

    assertThat(row.get("accessible"), is(false));
    assertThat(row.get("badge"), is(equalTo(RatedPlanCards.UNAVAILABLE_BADGE)));
    assertThat(row.get("name"), is(equalTo("Bariloche")));
    assertThat(row.get("ratingAverage"), is(nullValue()));
    assertThat(row.get("ratingCount"), is(equalTo(0L)));
  }
}
