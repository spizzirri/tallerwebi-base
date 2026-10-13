package com.valhalla.e2e.views;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

public class WebPage {

  protected Page page;

  public WebPage(Page page) {
    this.page = page;
  }

  public URL getCurrentUrl() throws MalformedURLException {
    return URI.create(page.url()).toURL();
  }

  /** Visible content of the document title. */
  public String getTitle() {
    return page.title();
  }

  /**
   * Waits until the browser sits on {@code path}. Tolerates a query string as well as the
   * {@code ;jsessionid} suffix: Spring Security lands on {@code ?logout=true} and {@code
   * ?error=true} after these flows, and a pattern that only allowed the session id would time out
   * on them instead of matching.
   *
   * <p>The path has to start right after the authority, which is what {@code [^/]*} pins down. A
   * bare {@code .*} prefix would let {@code waitForPath("/plans")} be satisfied by
   * {@code /admin/plans}, so a wrong redirect would pass. That assumes the app is deployed at the
   * context root, which is how failsafe starts Jetty; under a context path the caller has to pass
   * {@code /context/...} too.
   */
  public void waitForPath(String path) {
    page.waitForURL(
      Pattern.compile("[a-zA-Z][a-zA-Z0-9+.-]*://[^/]*" + Pattern.quote(path) + "(?:[?;][^#]*)?$")
    );
  }

  /**
   * Base URL for E2E navigation. Failsafe derives this from ${jetty.port} (see pom.xml), so
   * there is no local default: one that silently said 8080 would let the tests keep hitting
   * the old port after the server moved.
   */
  public String baseUrl() {
    String baseUrl = System.getProperty("e2e.baseUrl");
    if (baseUrl == null) {
      throw new IllegalStateException(
        "e2e.baseUrl is not set. Run through failsafe, or pass -De2e.baseUrl when running" +
        " from an IDE."
      );
    }
    return baseUrl;
  }

  public void navigate(String url) {
    page.navigate(url);
  }

  /**
   * Brand text of the shared navbar, which every view carries. Anonymous state: the plain brand
   * and the Register and Login links — sin el link de planes públicos, que es de sesión iniciada.
   */
  public String getNavbarBrand() {
    return this.getElementText("nav a.navbar-brand").replaceAll("\\s+", " ").trim();
  }

  /**
   * First initial of the signed-in user's real first name, present only once a session exists.
   * This is the branch of the navbar fragment that {@link #getNavbarBrand()} cannot see, because
   * a logged-out visitor never renders it.
   */
  public String getNavbarAvatarInitial() {
    return this.getElementText("nav .navbar-avatar").replaceAll("\\s+", " ").trim();
  }

  /**
   * Visible text of every navbar link and button, in document order. Pinned by the callers
   * instead of filtered: a menu item pointing at a route nobody maps still renders perfectly,
   * so the only way to catch it is to name the items the navbar is allowed to have.
   *
   * <p>Whitespace is collapsed the same way {@link #getNavbarAvatarInitial()} does it, because
   * {@code allTextContents()} returns the raw markup text and a template that wraps the label
   * across lines would otherwise change the expected value.
   *
   * <p>Pinned to the shared navbar rather than to any {@code <nav>}: the landing page carries a
   * footer nav of its own (links to /, /explore, /plans and /auth/register), and a bare
   * {@code nav a} would fold those links into the set the caller is pinning. The shared navbar is
   * the direct child of {@code <body>} in layouts/base :: layout; the footer nav lives inside
   * {@code <main>}.
   */
  public List<String> getNavbarItems() {
    return page
      .locator("body > nav a, body > nav button")
      .allTextContents()
      .stream()
      .map(text -> text.replaceAll("\\s+", " ").trim())
      .toList();
  }

  protected String getElementText(String cssSelector) {
    return this.getElement(cssSelector).textContent();
  }

  protected void clickElement(String cssSelector) {
    this.getElement(cssSelector).click();
  }

  protected void typeIntoElement(String cssSelector, String text) {
    this.getElement(cssSelector).fill(text);
    // Dispatch input event so Vue v-model picks up the value
    this.getElement(cssSelector)
      .evaluate("el => el.dispatchEvent(new Event('input', {bubbles: true}))");
  }

  private Locator getElement(String cssSelector) {
    return page.locator(cssSelector);
  }
}
