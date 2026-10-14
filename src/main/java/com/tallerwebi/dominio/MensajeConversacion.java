package com.tallerwebi.dominio;

public class MensajeConversacion {

  /* clases privadas para que no puedan ser modificadas aquí */
  private final String rol;
  private final String texto;

  /* constructor */
  public MensajeConversacion(String rol, String texto) {
    this.rol = rol;
    this.texto = texto;
  }

  /* getters y setters */
  public String getRol() {
    return rol;
  }

  public String getTexto() {
    return texto;
  }
}
