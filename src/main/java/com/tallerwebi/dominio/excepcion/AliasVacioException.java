package com.tallerwebi.dominio.excepcion;

public class AliasVacioException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public AliasVacioException(String message) {
    super(message);
  }
}
