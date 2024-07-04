package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class CalendarioEndToEndTest {

    private static Playwright playwright;
    private static Browser browser;
    private static BrowserContext context;
    private static Page page;

    @BeforeAll
    public static void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterAll
    public static void tearDown() {
        if (context != null) {
            context.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @Test
    public void testLoginAndNavigateToCalendario() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newPage();
            page.setDefaultTimeout(60000);

            page.navigate("http://tusitio.com");

            // Iniciar sesión
            page.fill("input[name='username']", "miUsuario");
            page.fill("input[name='password']", "miContraseña");
            page.click("button[type='submit']");

            // Navegar al calendario
            page.click("a[href='/calendario']");

            // Verificar que estamos en la página del calendario
            assertTrue(page.url().contains("/calendario"));

        } catch (PlaywrightException e) {
            System.out.println("El navegador se cerró inesperadamente: " + e.getMessage());
            // Opcional: Toma una captura de pantalla para análisis posterior
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("screenshot.png")));
            fail("El navegador se cerró inesperadamente: " + e.getMessage());
        }
    }

    @Test
    public void testAgregarItemRendimiento() {
        page.navigate("http://localhost:8081/spring/login");

        page.fill("input[name='username']", "testUser");
        page.fill("input[name='password']", "password");
        page.click("button[name='login']");

        page.navigate("http://localhost:8081/spring/calendario");

        page.fill("select[name='tipoRendimiento']", "ALTO");
        page.click("button[type='submit']");

        page.navigate("http://localhost:8081/spring/verProgreso");

        String text = page.locator("#datosItemRendimiento").textContent();
        assertTrue(text.contains("ALTO"));
    }
}
