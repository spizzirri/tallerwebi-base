package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaComentarios extends VistaWeb {

  public VistaComentarios(Page page) {
    super(page);
    page.navigate("localhost:8080/spring/comentarios");
  }

  public void escribirComentario(String contenido) {
    this.page.locator("#contenido").fill(contenido);
  }

  public void publicarComentario() {
    this.darClickEnElElemento("button[type='submit']");
  }

  public String obtenerMensajeDeExito() {
    return this.obtenerTextoDelElemento("p.alert-success");
  }

  public String obtenerMensajeDeError() {
    return this.obtenerTextoDelElemento("p.alert-danger");
  }

  public String obtenerPrimerComentarioDelListado() {
    return this.obtenerTextoDelElemento("#tabla-comentarios tbody tr:first-child td");
  }
}
