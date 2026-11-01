package com.valhalla.presentation.plan;

import com.valhalla.domain.plan.TransportMode;
import java.util.UUID;

/**
 * One itinerary row of the create-plan form, bound from {@code places[i].*}.
 *
 * <p>[SCH] La fila no lleva fecha ni hora: el servidor deriva el horario entero (D-7). Aceptarlas
 * aca volveria a abrir la puerta a itinerarios mixtos, con paradas tipeadas a mano mezcladas con las
 * calculadas.
 */
public class PlanPlaceRequest {

  private UUID placeId;

  private String description;

  private TransportMode transportModeToNext;

  // [SCH] Traslado hacia la parada siguiente fijado a mano; vacio en el form = null = calculado.
  private Integer travelMinutesToNext;

  public PlanPlaceRequest() {}

  public PlanPlaceRequest(UUID placeId, String description, TransportMode transportModeToNext) {
    this.placeId = placeId;
    this.description = description;
    this.transportModeToNext = transportModeToNext;
  }

  public UUID getPlaceId() {
    return placeId;
  }

  public void setPlaceId(UUID placeId) {
    this.placeId = placeId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public TransportMode getTransportModeToNext() {
    return transportModeToNext;
  }

  public void setTransportModeToNext(TransportMode transportModeToNext) {
    this.transportModeToNext = transportModeToNext;
  }

  public Integer getTravelMinutesToNext() {
    return travelMinutesToNext;
  }

  public void setTravelMinutesToNext(Integer travelMinutesToNext) {
    this.travelMinutesToNext = travelMinutesToNext;
  }
}
