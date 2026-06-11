package com.tallerwebi.dominio.excepcion;

public class LoginGoogleException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public LoginGoogleException(String message) {
    super(message);
  }

  public LoginGoogleException(String message, Throwable cause) {
    super(message, cause);
  }
}
