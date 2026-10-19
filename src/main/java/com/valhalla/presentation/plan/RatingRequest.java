package com.valhalla.presentation.plan;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * [CAL] Form del widget de estrellas: solo el puntaje. El email viaja en la sesion, nunca en el
 * form; el plan viene del path.
 */
public class RatingRequest {

  @NotNull(message = "Elegí una cantidad de estrellas")
  @Min(value = 1, message = "La calificación mínima es 1 estrella")
  @Max(value = 5, message = "La calificación máxima es 5 estrellas")
  private Integer score;

  public RatingRequest() {}

  public RatingRequest(Integer score) {
    this.score = score;
  }

  public Integer getScore() {
    return score;
  }

  public void setScore(Integer score) {
    this.score = score;
  }
}
