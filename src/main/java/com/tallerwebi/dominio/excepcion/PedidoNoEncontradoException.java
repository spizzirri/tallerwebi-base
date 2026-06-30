package com.tallerwebi.dominio.excepcion;

public class PedidoNoEncontradoException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public PedidoNoEncontradoException(String message) {
    super(message);
  }
}
