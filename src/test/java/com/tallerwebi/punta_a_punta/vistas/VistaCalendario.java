package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;

public class VistaCalendario extends VistaWeb {

    public VistaCalendario(Page page) {
        super(page);
        page.navigate("localhost:8081/spring/calendario");
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
        Locator locator = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Guardar"));
        locator.waitFor();
        System.out.println("Haciendo click en guardar");
        locator.click();

        // Esperar hasta que la URL cambie
        page.waitForURL(url -> url.toString().contains("/verProgreso"), new Page.WaitForURLOptions().setTimeout(10000)); // Esperar hasta 10 segundos
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
