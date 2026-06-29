package com.tallerwebi.dominio.excepcion;

public class AliasInvalidoException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public AliasInvalidoException(String message) {
    super(message);
  }
}
