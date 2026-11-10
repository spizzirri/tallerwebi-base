package com.valhalla.presentation.plan;

import java.math.BigDecimal;

public class PlanBudgetRequest {

  private BigDecimal minimum;
  private BigDecimal maximum;
  private String notes;

  public BigDecimal getMinimum() {
    return minimum;
  }

  public void setMinimum(BigDecimal minimum) {
    this.minimum = minimum;
  }

  public BigDecimal getMaximum() {
    return maximum;
  }

  public void setMaximum(BigDecimal maximum) {
    this.maximum = maximum;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }
}
