package com.valhalla.domain.plan;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

/** Estimated ARS spending per person for the entire itinerary. */
@Embeddable
public class PlanBudget {

  @Column(name = "budget_min", precision = 12, scale = 2)
  private BigDecimal minimum;

  @Column(name = "budget_max", precision = 12, scale = 2)
  private BigDecimal maximum;

  @Column(name = "budget_notes", length = 500)
  private String notes;

  public PlanBudget() {}

  public PlanBudget(BigDecimal minimum, BigDecimal maximum, String notes) {
    if ((minimum == null) != (maximum == null)) {
      throw new IllegalArgumentException("Completar ambos importes o dejar ambos vacios.");
    }
    if (minimum != null) {
      validateAmount(minimum);
      validateAmount(maximum);
      if (maximum.compareTo(minimum) < 0) {
        throw new IllegalArgumentException("El maximo debe ser mayor o igual al minimo.");
      }
    }
    this.minimum = minimum;
    this.maximum = maximum;
    this.notes = normalizeNotes(notes);
  }

  private static String normalizeNotes(String notes) {
    if (notes != null && notes.length() > 500) {
      throw new IllegalArgumentException("La aclaracion admite hasta 500 caracteres.");
    }
    return notes == null ? notes : notes.strip();
  }

  private static void validateAmount(BigDecimal amount) {
    if (
      amount.signum() < 0 ||
      amount.compareTo(new BigDecimal("9999999999.99")) > 0 ||
      amount.scale() > 2
    ) {
      throw new IllegalArgumentException("Importe fuera de rango o con mas de dos decimales.");
    }
  }

  public BigDecimal getMinimum() {
    return minimum;
  }

  public BigDecimal getMaximum() {
    return maximum;
  }

  public String getNotes() {
    return notes;
  }

  public boolean isEstimated() {
    return minimum != null && maximum != null;
  }

  public boolean isRange() {
    return isEstimated() && minimum.compareTo(maximum) != 0;
  }
}
