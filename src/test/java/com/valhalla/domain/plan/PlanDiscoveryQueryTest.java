package com.valhalla.domain.plan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests of {@link PlanDiscoveryQuery}: el parseo tolerante de parametros crudos y
 * {@code hasFilters()}. Sin Spring y sin base de datos.
 */
public class PlanDiscoveryQueryTest {

  @Test
  public void U14_sortDesconocidoCaeAPopular() {
    PlanDiscoveryQuery query = PlanDiscoveryQuery.parse(null, null, null, "nope");

    assertThat(query.sort(), is(PlanDiscoveryQuery.Sort.POPULAR));
  }

  @Test
  public void U15_categoriaDesconocidaSeIgnora() {
    PlanDiscoveryQuery query = PlanDiscoveryQuery.parse(null, "NOPE", null, null);

    assertThat(query.category(), is(nullValue()));
  }

  @Test
  public void U16_whenDesconocidoCaeAAll() {
    PlanDiscoveryQuery query = PlanDiscoveryQuery.parse(null, null, "nope", null);

    assertThat(query.when(), is(PlanDiscoveryQuery.When.ALL));
  }

  @Test
  public void parseToleraNullYBlancoEnTodosLosParametros() {
    PlanDiscoveryQuery empty = PlanDiscoveryQuery.parse(null, null, null, null);
    assertThat(empty.text(), is(""));
    assertThat(empty.category(), is(nullValue()));
    assertThat(empty.when(), is(PlanDiscoveryQuery.When.ALL));
    assertThat(empty.sort(), is(PlanDiscoveryQuery.Sort.POPULAR));

    PlanDiscoveryQuery blank = PlanDiscoveryQuery.parse("   ", "   ", "   ", "   ");
    assertThat(blank.text(), is(""));
    assertThat(blank.category(), is(nullValue()));
    assertThat(blank.when(), is(PlanDiscoveryQuery.When.ALL));
    assertThat(blank.sort(), is(PlanDiscoveryQuery.Sort.POPULAR));
  }

  @Test
  public void U17_hasFiltersEsFalsoSinTextoNiCategoriaYConWhenAll() {
    PlanDiscoveryQuery query = PlanDiscoveryQuery.parse("   ", null, "all", "date");

    assertThat(query.hasFilters(), is(false));
  }

  @Test
  public void U18_hasFiltersEsVerdaderoConCualquieraDeLosTresFiltros() {
    assertThat(PlanDiscoveryQuery.parse("museo", null, null, null).hasFilters(), is(true));
    assertThat(PlanDiscoveryQuery.parse(null, "BAR", null, null).hasFilters(), is(true));
    assertThat(PlanDiscoveryQuery.parse(null, null, "upcoming", null).hasFilters(), is(true));
  }
}
