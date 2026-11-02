package com.valhalla.e2e;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.Tracing;
import com.valhalla.e2e.views.LoginPage;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;

/**
 * Shared Playwright lifecycle for the E2E suite: one launch site, one context and page per test,
 * and a screenshot plus trace kept whenever a test fails.
 *
 * <p>Headless and pacing come from system properties, so watching a run no longer means editing
 * test code:
 *
 * <pre>
 *   mvn failsafe:integration-test failsafe:verify "-De2e.headed=true" "-De2e.slowMo=300"
 * </pre>
 *
 * <p>Failure artifacts land in {@code target/e2e-artifacts/}, which the CI e2e job uploads.
 */
abstract class E2eBase {

  private static final boolean HEADED = Boolean.getBoolean("e2e.headed");
  private static final int SLOW_MO = Integer.getInteger("e2e.slowMo", 0);
  private static final Path ARTIFACT_DIR = Path.of("target", "e2e-artifacts");

  static Playwright playwright;
  static Browser browser;

  BrowserContext context;
  Page page;

  @BeforeAll
  static void openBrowser() {
    playwright = Playwright.create();
    // --hide-scrollbars: los viewports del suite (320x568, 390x844) emulan telefonos, cuyos
    // scrollbars son overlay y no ocupan layout. Sin este flag Chromium de escritorio reserva
    // 15px de scrollbar vertical y el ancho util queda en 305px, lo que hace medir celdas del
    // calendario 1px por debajo del target tactil en CalendarE2E por un artefacto del
    // entorno, no del producto.
    browser =
      playwright
        .chromium()
        .launch(
          new BrowserType.LaunchOptions()
            .setHeadless(!HEADED)
            .setSlowMo(SLOW_MO)
            .setArgs(List.of("--hide-scrollbars"))
        );
  }

  @AfterAll
  static void closeBrowser() {
    playwright.close();
  }

  @BeforeEach
  void openContext() {
    ResetDatabase.cleanDatabase();
    newContext();
  }

  /**
   * Owns teardown as well as artifact capture, because the context has to stay open past the test
   * method and {@code TestWatcher} is the only hook that fires while it still is.
   *
   * <p>An {@code @AfterEach} method cannot do this: it runs before {@code testFailed}, so the page
   * is already closed by the time there is anything to capture. Nor can an after-each callback spot
   * the failure — {@code ExtensionContext.getExecutionException()} is still empty there, and an
   * {@code AfterEachCallback} declared on a superclass is never invoked.
   */
  @RegisterExtension
  final TestWatcher lifecycle = new TestWatcher() {
    @Override
    public void testFailed(ExtensionContext test, Throwable cause) {
      saveFailureArtifacts(test);
      closeContext();
    }

    @Override
    public void testSuccessful(ExtensionContext test) {
      closeContext();
    }

    @Override
    public void testAborted(ExtensionContext test, Throwable cause) {
      closeContext();
    }
  };

  private void closeContext() {
    context.close();
  }

  /** Starts a fresh context and page, for a test that has to switch session mid-test. */
  void newContext() {
    context = browser.newContext();
    // Hermetic suite: a slow or unreachable fonts.googleapis.com must not postpone the
    // head scripts. Empty CSS also means fonts.gstatic.com is never requested.
    context.route(
      "https://fonts.googleapis.com/**",
      route -> route.fulfill(new Route.FulfillOptions().setContentType("text/css").setBody(""))
    );
    context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));
    page = context.newPage();
  }

  /**
   * Signs the current page in as the seed admin and returns the page object already sitting on
   * {@code /admin/home}. Both admin-facing suites need this as arrange, so the credentials live
   * here instead of twice: {@code openContext} wipes the database, which makes the seed the only
   * account guaranteed to exist.
   */
  LoginPage signInAsAdmin() {
    LoginPage login = new LoginPage(page);
    login.typeEmail("test@unlam.edu.ar");
    login.typePassword("password");
    login.clickSignIn();
    login.waitForPath("/admin/home");
    return login;
  }

  /**
   * ISO date {@code days} from now, computed at call time: the create wizard's date input renders
   * {@code min=${today}} from the server's own {@code LocalDate.now()}, so a hardcoded literal
   * eventually falls below the min and {@code nextPlanStep()} silently refuses to advance past
   * step 1 (the carousel track then covers the later steps and Playwright times out).
   */
  protected static String isoDateIn(int days) {
    return LocalDate.now().plusDays(days).toString();
  }

  /** The {@code dd/MM/yyyy} label the app renders for an ISO date, to keep assertions in sync. */
  protected static String dateLabel(String isoDate) {
    return LocalDate.parse(isoDate).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
  }

  private void saveFailureArtifacts(ExtensionContext test) {
    Path dir = ARTIFACT_DIR.resolve(artifactName(test));
    try {
      page.screenshot(
        new Page.ScreenshotOptions().setPath(dir.resolve("screenshot.png")).setFullPage(true)
      );
      context.tracing().stop(new Tracing.StopOptions().setPath(dir.resolve("trace.zip")));
    } catch (RuntimeException e) {
      // A browser that already died must not replace the real test failure with a harness one.
      System.err.println(
        "Could not save E2E artifacts for " + test.getDisplayName() + ": " + e.getMessage()
      );
    }
  }

  private String artifactName(ExtensionContext test) {
    String className = test.getTestClass().map(Class::getSimpleName).orElse("E2E");
    String methodName = test.getTestMethod().map(Method::getName).orElse("unknown");
    return className + "-" + methodName;
  }
}
