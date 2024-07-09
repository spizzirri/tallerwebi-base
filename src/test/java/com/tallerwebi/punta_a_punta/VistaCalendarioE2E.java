package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import com.tallerwebi.punta_a_punta.vistas.VistaCalendario;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

public class VistaCalendarioE2E {

    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    VistaCalendario vistaCalendario;

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
        Page page = context.newPage();
        vistaCalendario = new VistaCalendario(page);
    }

    @AfterEach
    void cerrarContexto() {
        context.close();
    }

    @Test
    void deberiaMostrarTituloDelCalendario() {
        String titulo = vistaCalendario.obtenerTituloDelCalendario();
        assertThat("Calendario", equalToIgnoringCase(titulo));
    }

    @Test
    void deberiaAgregarUnEventoAlCalendario() {
        vistaCalendario.seleccionarFecha("2024-07-04");
        vistaCalendario.agregarEvento("Reunión de prueba");
        vistaCalendario.darClickEnAgregarEvento();
        String mensaje = vistaCalendario.obtenerMensajeDeConfirmacion();
        assertThat("Evento agregado exitosamente", equalToIgnoringCase(mensaje));
    }
}
