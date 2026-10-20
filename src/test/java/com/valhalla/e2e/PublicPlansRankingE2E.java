package com.valhalla.e2e;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import com.microsoft.playwright.Locator;
import com.valhalla.e2e.views.PlansPage;
import com.valhalla.e2e.views.RegisterPage;
import com.valhalla.e2e.views.WebPage;
import com.valhalla.presentation.shared.LoginRedirects;
import java.net.URI;
import org.junit.jupiter.api.Test;

/**
 * [TOP] E-01 (17-TOP.md): the feed ranks public plans by popularity, so a plan with real ratings
 * leads one without any.
 *
 * <p>The fixture is built through the UI, like {@link RatingE2E}: {@code E2eBase} wipes the database
 * before every test and re-seeds only the admin and the places, while
 * {@code PublicPlanDataSeeder} is off outside {@code docker-dev.sh}. The admin creates both plans,
 * a freshly registered visitor rates one of them, and the feed that visitor sees must put the rated
 * plan first.
 *
 * <p>The unrated plan is created first, sorts first by name and carries the earlier date, so any
 * ordering that ignored ratings would leave it on top: only the popularity rank can move it down.
 *
 * <p>Names and descriptions are ASCII on purpose. The app answers with {@code
 * text/html;charset=iso-8859-1}, the browser builds the form body in Latin-1 and Jetty rejects an
 * accented field with a 400 (the pre-existing encoding bug documented in {@link
 * PublicPlansListE2E}).
 */
public class PublicPlansRankingE2E extends E2eBase {

  private static final String UNRATED_PLAN = "Asado sin votos E2E";
  private static final String UNRATED_DESCRIPTION = "Un plan que nadie califico todavia";
  private static final String UNRATED_DATE = "2026-11-01";
  private static final String RATED_PLAN = "Milonga calificada E2E";
  private static final String RATED_DESCRIPTION = "Un plan con una calificacion real";
  private static final String RATED_DATE = "2026-12-31";
  private static final String VISITOR_EMAIL = "visitante.top@unlam.edu.ar";
  private static final String VISITOR_PASSWORD = "secret123";

  @Test
  void T_TOP_001_elPlanCalificadoLideraElFeed() {
    // --- arrange: both public plans come from the admin's session, through the create modal.
    signInAsAdmin();
    PlansPage plans = new PlansPage(page);
    plans.createPublicPlanViaExplore(UNRATED_PLAN, UNRATED_DESCRIPTION, UNRATED_DATE);
    plans.createPublicPlanViaExplore(RATED_PLAN, RATED_DESCRIPTION, RATED_DATE);
    // The public URL is captured while the detail page is open: the admin session navigates away
    // afterwards and the id is only there.
    String ratedPlanUrl = publicUrlOf(plans);

    // --- act: a different visitor registers and rates the second plan 5 stars.
    newContext();
    givenARegisteredVisitor();
    page.navigate(ratedPlanUrl);
    rateFiveStars();
    assertThat(page.locator("body").innerText(), containsString("1 calificación"));

    // --- assert: the rated plan leads the feed; the unrated one drops to second place.
    WebPage web = new WebPage(page);
    web.navigate(web.baseUrl() + LoginRedirects.USER_LANDING);
    Locator cards = page.locator("main article");
    assertThat(cards.count(), is(2));
    assertThat(
      "el plan calificado va primero",
      cards.first().innerText(),
      containsString(RATED_PLAN)
    );
    assertThat(cards.nth(1).innerText(), containsString(UNRATED_PLAN));
    // AC-07: the card keeps showing the aggregate that earned it the first place.
    assertThat(cards.first().innerText(), containsString("1 calificación"));
  }

  /** The public URL of the plan whose detail page the admin session has open. */
  private String publicUrlOf(PlansPage plans) {
    String planId = URI.create(page.url()).getPath().replaceAll(".*/", "");
    return plans.baseUrl() + "/plans/" + planId + "/public";
  }

  /** Registers a clean visitor in the current context; registration opens the session. */
  private void givenARegisteredVisitor() {
    RegisterPage register = new RegisterPage(page);
    register.typeFirstName("Valeria");
    register.typeLastName("Ranking");
    register.typeEmail(VISITOR_EMAIL);
    register.typePassword(VISITOR_PASSWORD);
    register.typeConfirmPassword(VISITOR_PASSWORD);
    register.clickRegister();
    // Against the shared constant, not a literal: the landing can change without editing this.
    new WebPage(page).waitForPath(LoginRedirects.USER_LANDING);
  }

  /**
   * One click on the first star option votes 5: the radio group renders 5..1 and the change
   * handler submits the form, so the wait is the radio re-rendered checked by the POST response.
   */
  private void rateFiveStars() {
    page.locator(".rating-star-option").first().click();
    page.waitForSelector("input[name='score'][value='5']:checked");
  }
}
