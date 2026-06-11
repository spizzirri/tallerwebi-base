package com.tallerwebi.presentacion;

public class GeminiDto {

  private String pregunta;
  private String respuesta;
  private String reglaAdicional;
  private String contextoActual;

  public String getPregunta() {
    return pregunta;
  }

  public void setPregunta(String pregunta) {
    this.pregunta = pregunta;
  }

  public String getRespuesta() {
    return respuesta;
  }

  public void setRespuesta(String respuesta) {
    this.respuesta = respuesta;
  }

  public String getReglaAdicional() {
    return reglaAdicional;
  }

  public void setReglaAdicional(String reglaAdicional) {
    this.reglaAdicional = reglaAdicional;
  }

  public String getContextoActual() {
    return contextoActual;
  }

  public void setContextoActual(String contextoActual) {
    this.contextoActual = contextoActual;
  }
}
