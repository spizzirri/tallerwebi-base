package com.tallerwebi.dominio;

import java.util.List;

/** Los turnos de una mascota separados en próximos y pasados. */
public class HistorialTurnos {

  private final List<Turno> proximos;
  private final List<Turno> pasados;

  public HistorialTurnos(List<Turno> proximos, List<Turno> pasados) {
    this.proximos = proximos;
    this.pasados = pasados;
  }

  public List<Turno> getProximos() {
    return proximos;
  }

  public List<Turno> getPasados() {
    return pasados;
  }
}
