package com.tallerwebi.dominio.excepcion;

public class ComentarioInvalido extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ComentarioInvalido(String mensaje) {
    super(mensaje);
  }
}
