package com.tallerwebi.dominio.excepcion;

public class AliasExistenteException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public AliasExistenteException(String message) {
    super(message);
  }
}
