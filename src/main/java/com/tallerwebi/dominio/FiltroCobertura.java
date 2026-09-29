package com.tallerwebi.dominio;

public enum FiltroCobertura {
  TODOS,
  INCLUIDOS,
  NO_INCLUIDOS;

  public boolean acepta(ItemCobertura item) {
    switch (this) {
      case INCLUIDOS:
        return item.isIncluido();
      case NO_INCLUIDOS:
        return !item.isIncluido();
      default:
        return true;
    }
  }
}
