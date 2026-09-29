package com.tallerwebi.dominio.excepcion;

public class TurnoInvalido extends Exception {

  /* Identificador para la serialización de la clase, requerido por PMD en excepciones */
  private static final long serialVersionUID = 1L;

  public TurnoInvalido(String mensaje) {
    super(mensaje);
  }
}
