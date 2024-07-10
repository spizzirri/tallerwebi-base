package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;

public class VistaCalendario extends VistaWeb {

    public VistaCalendario(Page page) {
        super(page);
        System.out.println("Navegando a la página del calendario...");
        page.navigate("http://localhost:8081/spring/calendario");
        System.out.println("Navegación completada.");
    }

    public String obtenerTextoDeLaPagina() {
        Locator locator = page.locator("h2");
        locator.waitFor();
        String texto = locator.innerText();
        System.out.println("Texto del h2: " + texto);
        return texto;
    }

    public void seleccionarTipoRendimiento(String valor) {
        Locator locator = page.locator("select.select-estilo");
        System.out.println("Esperando que el selector de estilo sea visible...");
        locator.waitFor(new Locator.WaitForOptions().setTimeout(60000)); // Aumenta el tiempo de espera a 60 segundos
        System.out.println("Seleccionando opción: " + valor);
        locator.selectOption(valor);
    }

    public void darClickEnGuardar() {
        Locator locator = page.locator("button.boton-guardar");
        locator.waitFor();
        System.out.println("Haciendo click en guardar");
        locator.click();
    }

    public String obtenerMensajeDeError() {
        Locator locator = page.locator(".alert.alert-danger p");
        locator.waitFor();
        String mensaje = locator.innerText();
        System.out.println("Mensaje de error: " + mensaje);
        return mensaje;
    }

    public String obtenerURLActual() {
        String url = page.url();
        System.out.println("URL actual: " + url);
        return url;
    }
}
