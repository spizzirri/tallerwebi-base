package com.valhalla.presentation.plan;

// Strings avoid introducing a JavaTime Jackson module absent from the existing MVC configuration.
// transportModeToNext arrives as the enum name and the controller converts it.
// travelMinutesToNext binds as Integer like durationMinutes: a non-numeric value fails to
// deserialize and the controller's handler answers 400.
public record PlanPlaceUpdateRequest(
  String visitDate,
  String visitTime,
  Integer durationMinutes,
  String transportModeToNext,
  Integer travelMinutesToNext
) {}
