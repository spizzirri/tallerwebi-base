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
        System.out.println("Navegador abierto.");
    }

    @AfterAll
    static void cerrarNavegador() {
        playwright.close();
        System.out.println("Navegador cerrado.");
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
    void deberiaMostrarTituloDeLaPagina() {
        String texto = vistaCalendario.obtenerTextoDeLaPagina();
        assertThat("¿Cómo fue tu entrenamiento hoy?", equalToIgnoringCase(texto));
    }

    @Test
    void deberiaMostrarErrorSiNoSeCompletaElFormulario() {
        vistaCalendario.darClickEnGuardar();
        String texto = vistaCalendario.obtenerMensajeDeError();
        assertThat("Error: debe completar todos los campos", equalToIgnoringCase(texto));
    }

    @Test
    void deberiaGuardarItemRendimiento() {
        vistaCalendario.seleccionarTipoRendimiento("ALTO");
        vistaCalendario.darClickEnGuardar();
        String url = vistaCalendario.obtenerURLActual();
        assertThat(url, containsStringIgnoringCase("/verProgreso"));
    }
}
