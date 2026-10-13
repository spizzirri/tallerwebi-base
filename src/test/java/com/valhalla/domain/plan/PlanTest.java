package com.valhalla.domain.plan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import com.valhalla.domain.user.User;
import java.lang.reflect.Field;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Behaviour test of {@link Plan#updateFrom(Plan)} and of the entity's defensive null guards.
 *
 * <p>Lives in {@code domain/plan/} next to the entity, which is the documented exception to the
 * placement rule of docs/testing.md: this is entity behaviour, not a test of a port.
 *
 * <p>This is the regression test for the {@code PUT} defect. The update used to go through
 * {@code merge()} with a detached plan built out of the form fields, which blanked every column the
 * form does not send — the owner and the share code among them. {@code updateFrom} copies the four
 * fields the form owns and nothing else.
 */
public class PlanTest {

  private static final Long PLAN_ID = 42L;
  private static final String SHORT_CODE = "ABCD1234";

  private User administrator;
  private Plan plan;

  @BeforeEach
  public void init() {
    this.administrator = new User();
    this.administrator.setId(7L);
    this.administrator.setEmail("dueno@test.com");

    this.plan = new Plan();
    this.plan.setId(PLAN_ID);
    this.plan.setName("Nombre viejo");
    this.plan.setDescription("Descripcion vieja");
    this.plan.setEventDate(LocalDate.of(2026, 1, 1));
    this.plan.setIsPublic(false);
    this.plan.setShortCode(SHORT_CODE);
    this.plan.setAdministrator(this.administrator);
  }

  @Test
  public void T_PLN_050_updateFrom_cambiaLosCuatroCamposQueMandaElForm() {
    // given
    Plan changes = new Plan();
    changes.setName("Nombre nuevo");
    changes.setDescription("Descripcion nueva");
    changes.setEventDate(LocalDate.of(2026, 12, 31));
    changes.setIsPublic(true);

    // when
    this.plan.updateFrom(changes);

    // then
    assertThat(this.plan.getName(), is(equalTo("Nombre nuevo")));
    assertThat(this.plan.getDescription(), is(equalTo("Descripcion nueva")));
    assertThat(this.plan.getEventDate(), is(equalTo(LocalDate.of(2026, 12, 31))));
    assertThat(this.plan.getIsPublic(), is(true));
  }

  @Test
  public void T_PLN_051_updateFrom_noCambiaIdShortCodeNiAdministrator() {
    // given: a form post carries none of these, and a hostile one could set them
    Plan changes = new Plan();
    changes.setName("Nombre nuevo");
    changes.setId(999L);
    changes.setShortCode("IGNORADO");
    changes.setAdministrator(new User());

    // when
    this.plan.updateFrom(changes);

    // then
    assertThat(this.plan.getId(), is(equalTo(PLAN_ID)));
    assertThat(this.plan.getShortCode(), is(equalTo(SHORT_CODE)));
    assertThat(this.plan.getAdministrator(), is(sameInstance(this.administrator)));
  }

  @Test
  public void T_PLN_052_updateFrom_conFormVacioDejaVisibilidadEnFalse() {
    // given: an unchecked checkbox posts nothing, so the DTO hands over a null Boolean
    Plan changes = new Plan();
    changes.setName("Solo nombre");
    changes.setIsPublic(null);

    // when
    this.plan.updateFrom(changes);

    // then
    assertThat(this.plan.getName(), is(equalTo("Solo nombre")));
    assertThat(this.plan.getDescription(), is(nullValue()));
    assertThat(this.plan.getEventDate(), is(nullValue()));
    assertThat(this.plan.getIsPublic(), is(false));
    assertThat(this.plan.getShortCode(), is(equalTo(SHORT_CODE)));
  }

  @Test
  public void T_PLN_053_gettersDefensivosManejanScheduleNulo() throws Exception {
    // given: schedule is initialised inline, so the public API can never null it — only
    // Hibernate can, when it hydrates a row whose schedule is absent. The defensive
    // getters must answer null instead of throwing, and a setter must rebuild the
    // schedule on demand. Reaching that state needs reflection, hence the test lives
    // here rather than in a port test.
    nullOutSchedule(this.plan);

    // then: both getters fall back to null rather than NPE
    assertThat(this.plan.getEventDate(), is(nullValue()));
    assertThat(this.plan.getEventTime(), is(nullValue()));

    // when: a setter runs, ensureSchedule() rebuilds the object instead of NPE
    this.plan.setEventDate(LocalDate.of(2026, 5, 5));

    // then
    assertThat(this.plan.getEventDate(), is(equalTo(LocalDate.of(2026, 5, 5))));
    assertThat(this.plan.getEventTime(), is(nullValue()));
  }

  @Test
  public void T_PLN_054_predicadosDeAccesoNoLanzanConEntradaNula() {
    // given: the clone access-check asks isAdministrator/isParticipant with whatever the
    // session holds, which can be null. The short-circuit guards must answer false, and
    // branch coverage pins each disjunct so a later refactor cannot silently drop one.
    Plan planWithoutAdministrator = new Plan();

    // then: null email short-circuits before touching the administrator
    assertThat(this.plan.isAdministrator(null), is(false));
    assertThat(this.plan.isParticipant(null), is(false));

    // then: a missing administrator (fresh plan) short-circuits before equals()
    assertThat(planWithoutAdministrator.isAdministrator("dueno@test.com"), is(false));
  }

  private static void nullOutSchedule(Plan plan) throws ReflectiveOperationException {
    Field field = Plan.class.getDeclaredField("schedule");
    field.setAccessible(true);
    field.set(plan, null);
  }
}
