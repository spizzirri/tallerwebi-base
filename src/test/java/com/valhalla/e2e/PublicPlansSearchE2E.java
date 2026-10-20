package com.valhalla.e2e;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.microsoft.playwright.Locator;
import com.valhalla.e2e.views.PlansPage;
import com.valhalla.e2e.views.WebPage;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * [SEARCH] E-01 (18-SEARCH.md): the feed form at {@code GET /for-you} filters by text, sorts the
 * result and clears itself, without leaving the server-rendered page.
 *
 * <p>The assertion that matters for the state machine is the last pair: with a text filter and no
 * match the page shows "No encontramos planes con esos filtros." plus the way out, and never the
 * historical empty feed. The historical state itself is already pinned by {@link
 * PublicPlansListE2E}, so it is only asserted here as absent.
 *
 * <p>{@code E2eBase} wipes the database before the test, so the three public plans come from the
 * admin's UI session and the observer of the feed is a fresh anonymous context: the admin's own
 * plans never appear in the admin's feed. Names and descriptions are ASCII for the same reason as
 * in {@link PublicPlansRankingE2E}.
 */
public class PublicPlansSearchE2E extends E2eBase {

  private static final String FIRST_PLAN = "Alfa Noche E2E";
  private static final String FIRST_DESCRIPTION = "Propuesta uno";
  private static final String FIRST_DATE = "2027-03-01";
  private static final String SECOND_PLAN = "Zeta Noche E2E";
  private static final String SECOND_DESCRIPTION = "Propuesta dos";
  private static final String SECOND_DATE = "2026-11-20";
  private static final String OTHER_PLAN = "Parrilla del barrio E2E";
  private static final String OTHER_DESCRIPTION = "Asado con amigos";
  private static final String OTHER_DATE = "2027-01-15";

  @Test
  void T_SEARCH_001_buscarOrdenarYLimpiarLosFiltrosVuelveAlFeedCompleto() {
    givenThreePublicPlans();
    newContext();
    WebPage web = new WebPage(page);
    web.navigate(web.baseUrl() + "/for-you");

    // Baseline: all three unrated plans, so popularity ties are broken by the earliest date.
    assertThat(cardNames(), contains(SECOND_PLAN, OTHER_PLAN, FIRST_PLAN));

    // --- AC-01: the text filter keeps only the plans whose name matches, case-insensitively.
    page.fill("input[name='q']", "noche");
    submitFilters("q=noche");
    assertThat(cardNames(), contains(SECOND_PLAN, FIRST_PLAN));
    assertThat(cardsMatching(OTHER_PLAN).count(), is(0));
    // The applied value comes back in the form (AC-08).
    assertThat(page.locator("input[name='q']").inputValue(), is("noche"));

    // --- AC-05: sort=name reorders the filtered set; the earlier date no longer decides.
    page.selectOption("select[name='sort']", "name");
    submitFilters("sort=name");
    assertThat(cardNames(), contains(FIRST_PLAN, SECOND_PLAN));
    assertThat(page.locator("select[name='sort']").inputValue(), is("name"));
    assertThat(page.locator("input[name='q']").inputValue(), is("noche"));

    // --- AC-09: filters with no match are not an empty feed: their own message and the way back.
    page.fill("input[name='q']", "zzz");
    submitFilters("q=zzz");
    assertThat(page.locator("main article").count(), is(0));
    String mainText = page.locator("main").innerText();
    assertThat(mainText, containsString("No encontramos planes con esos filtros."));
    assertThat(mainText, not(containsString("Todavía no hay planes públicos")));
    Locator clearFilters = page.locator("a:has-text('Limpiar filtros')");
    assertThat(clearFilters.count(), is(1));

    // --- "Limpiar filtros" drops the query string and returns the whole feed.
    clearFilters.click();
    page.waitForURL(Pattern.compile(".*/for-you$"));
    page.waitForLoadState();
    assertThat(page.locator("main article").count(), is(3));
    assertThat(cardNames(), contains(SECOND_PLAN, OTHER_PLAN, FIRST_PLAN));
    assertThat(page.locator("input[name='q']").inputValue(), is(""));
    assertThat(page.locator("select[name='sort']").inputValue(), is("popular"));
  }

  /** Creates the three public plans with the admin session, through the create modal. */
  private void givenThreePublicPlans() {
    signInAsAdmin();
    PlansPage plans = new PlansPage(page);
    plans.createPublicPlanViaExplore(FIRST_PLAN, FIRST_DESCRIPTION, FIRST_DATE);
    plans.createPublicPlanViaExplore(SECOND_PLAN, SECOND_DESCRIPTION, SECOND_DATE);
    plans.createPublicPlanViaExplore(OTHER_PLAN, OTHER_DESCRIPTION, OTHER_DATE);
  }

  /**
   * Submits the feed form and waits for the resulting URL. The fragment is a piece of the query
   * string that the previous URL does not contain, so the wait cannot be satisfied by the page
   * that is already open.
   */
  private void submitFilters(String queryFragment) {
    page.locator("form[action='/for-you'] button[type='submit']").click();
    page.waitForURL(Pattern.compile(".*for-you\\?.*" + queryFragment + ".*"));
    page.waitForLoadState();
  }

  /** Rendered card titles, in document order: the order the feed is asserting. */
  private List<String> cardNames() {
    return page.locator("main article h3").allInnerTexts();
  }

  private Locator cardsMatching(String name) {
    return page.locator("main article").filter(new Locator.FilterOptions().setHasText(name));
  }
}
