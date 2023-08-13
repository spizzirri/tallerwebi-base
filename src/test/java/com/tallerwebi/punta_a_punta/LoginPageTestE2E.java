package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
public class LoginPageTestE2E {

    static Playwright playwright;
    static Browser browser;

    BrowserContext context;
    Page page;

    @BeforeAll
    static void abrirNavegador() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        //browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(50));

    }

    @AfterAll
    static void cerrarNavegador() {
        playwright.close();
    }

    @BeforeEach
    void crearContextoYPagina() {
        context = browser.newContext();
        page = context.newPage();
        page.navigate("localhost:8080/spring");
    }

    @AfterEach
    void cerrarContexto() {
        context.close();
    }

    @Test
    void deberiaDecirUNLAMEnElNavbar() {
        String texto = page.locator("nav a.navbar-brand").textContent();
        assertThat("UNLAM", equalToIgnoringCase(texto));
    }

    @Test
    void deberiaDarUnErrorAlNoCompletarElLoginYTocarElBoton() {
        page.locator("#email").type("damian@unlam.edu.ar");
        page.locator("#password").type("unlam");
        page.locator("#btn-login").click();
        String texto = page.locator("p.alert.alert-danger").textContent();
        assertThat("Error Usuario o clave incorrecta", equalToIgnoringCase(texto));
    }

    @Test
    void deberiaNavegarAlHomeSiElUsuarioExiste() {
        page.locator("#email").type("test@unlam.edu.ar");
        page.locator("#password").type("test");
        page.locator("#btn-login").click();

        String url = page.url();
        assertThat(url, containsStringIgnoringCase("/spring/home"));
    }
}
