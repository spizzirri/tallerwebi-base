package com.tallerwebi.dominio.excepcion;

public class ProductoSinStockException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ProductoSinStockException(String message) {
    super(message);
  }
}
