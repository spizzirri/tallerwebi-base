package com.tallerwebi.dominio.excepcion;

public class FechaRetiroInvalidaException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public FechaRetiroInvalidaException(String message) {
    super(message);
  }
}
