package com.valhalla.presentation.plan;

/**
 * [PABM] Form del ABM de planes en el admin: reutiliza los campos comunes de {@link PlanRequest}
 * (nombre, fecha, hora de inicio, visibilidad) y agrega unicamente {@code active}, porque el admin
 * es quien decide la visibilidad del plan en los listados publicos. La ventana del dia no es un
 * campo del admin (AC-21): el controller la ignora.
 */
public class EditPlanRequest extends PlanRequest {

  // Sin default a proposito: en un POST el checkbox sin tildar no manda el param y el null
  // significa "apagado" (el controller lo normaliza); el tildado del form nuevo lo pone la vista.
  private Boolean active;

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }
}
