package com.valhalla.domain.exception;

/**
 * Thrown when deleting a place that is still referenced by at least one plan: the FK
 * {@code plan_places.place_id} has no cascade, so the delete must be refused, not attempted.
 */
public class PlaceInUseException extends RuntimeException {

  /* serialVersionUID for class serialization, required by PMD for exceptions */
  private static final long serialVersionUID = 1L;
}
