package com.tallerwebi.dominio.excepcion;

public class DatosDeRegistroInvalidos extends Exception {

  /* Identificador para la serialización de la clase, requerido por PMD en excepciones */
  private static final long serialVersionUID = 1L;

  public DatosDeRegistroInvalidos(String mensaje) {
    super(mensaje);
  }
}
