package com.tallerwebi.dominio;

/** Un servicio junto con si está incluido o no en el plan de una mascota. */
public class ItemCobertura {

  private final Servicio servicio;
  private final boolean incluido;

  public ItemCobertura(Servicio servicio, boolean incluido) {
    this.servicio = servicio;
    this.incluido = incluido;
  }

  public Servicio getServicio() {
    return servicio;
  }

  public boolean isIncluido() {
    return incluido;
  }
}
