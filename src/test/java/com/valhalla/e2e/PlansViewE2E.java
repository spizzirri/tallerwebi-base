package com.valhalla.e2e;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import com.valhalla.e2e.views.PlansPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Browser coverage of the plan CRUD.
 *
 * <p>E2eBase wipes the database before every test, so the seeded admin is the only account that
 * exists and it owns everything it creates. The session comes from {@link E2eBase#signInAsAdmin()}
 * instead of hardcoded URLs: {@code /plans} is behind Spring Security, and the old suite navigated
 * to it straight from {@code about:blank} and then asserted on whatever page it got.
 */
public class PlansViewE2E extends E2eBase {

  private static final String PLAN_NAME = "Viaje E2E de prueba";

  private PlansPage plans;

  @BeforeEach
  void signInAndOpenPlans() {
    signInAsAdmin();
    this.plans = new PlansPage(page);
  }

  @Test
  void T_PLN_014_crearPlan_loMuestraEnElListadoYEnElDetalle() {
    givenAPlanExists();

    this.plans.navigateToPlans();
    this.plans.waitForPath("/plans");
    assertThat(this.plans.getPlanCount(), is(1));
    assertThat(this.plans.hasPlanNamed(PLAN_NAME), is(true));

    this.plans.clickDetailOnRow(PLAN_NAME);
    this.plans.waitForDetailPath();
    assertThat(this.plans.getDetailHeading(), containsString(PLAN_NAME));
  }

  @Test
  void T_PLN_015_borrarDesdeLaListado_haceDesaparecerElPlan() {
    givenAPlanExists();

    this.plans.navigateToPlans();
    this.plans.waitForPath("/plans");
    assertThat(
      "the delete button is only meaningful with a row to delete",
      this.plans.getPlanCount(),
      is(1)
    );

    this.plans.deletePlanNamed(PLAN_NAME);
    this.plans.waitForPath("/plans");

    // Counting rows instead of grepping the markup: the plan name is also in the delete form's
    // action URL, so the old "the page does not contain the name" assertion passed even when the
    // row was still on screen.
    assertThat(this.plans.getPlanCount(), is(0));
    assertThat(this.plans.hasPlanNamed(PLAN_NAME), is(false));
  }

  private void givenAPlanExists() {
    this.plans.createPlanViaExplore(PLAN_NAME, "Un viaje de prueba", isoDateIn(30));
  }
}
