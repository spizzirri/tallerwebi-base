package com.valhalla.domain.plan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

public class PlanBudgetTest {

  @Test
  void absentAmountsAreNotAnEstimateOrRange() {
    PlanBudget budget = new PlanBudget();
    assertNull(budget.getMinimum());
    assertNull(budget.getMaximum());
    assertNull(budget.getNotes());
    assertFalse(budget.isEstimated());
    assertFalse(budget.isRange());
    PlanBudget notesOnly = new PlanBudget(null, null, "  Entradas aparte  ");
    assertFalse(notesOnly.isEstimated());
    assertEquals("Entradas aparte", notesOnly.getNotes());
  }

  @Test
  void equalAmountsIgnoreDecimalScaleAndZeroMeansFree() {
    PlanBudget fixed = new PlanBudget(new BigDecimal("2000.0"), new BigDecimal("2000.00"), null);
    assertTrue(fixed.isEstimated());
    assertFalse(fixed.isRange());
    PlanBudget free = new PlanBudget(BigDecimal.ZERO, BigDecimal.ZERO, null);
    assertTrue(free.isEstimated());
    assertFalse(free.isRange());
  }

  @Test
  void rangeAcceptsInclusiveAmountAndNoteLimits() {
    BigDecimal maximum = new BigDecimal("9999999999.99");
    String notes = "a".repeat(500);
    PlanBudget budget = new PlanBudget(BigDecimal.ZERO, maximum, notes);
    assertEquals(BigDecimal.ZERO, budget.getMinimum());
    assertEquals(maximum, budget.getMaximum());
    assertEquals(notes, budget.getNotes());
    assertTrue(budget.isRange());
  }

  @Test
  void eitherMissingBoundIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new PlanBudget(null, BigDecimal.ONE, null));
    assertThrows(IllegalArgumentException.class, () -> new PlanBudget(BigDecimal.ONE, null, null));
  }

  @Test
  void negativeOversizedAndOverpreciseAmountsAreRejectedAtEitherBound() {
    for (String invalid : new String[] { "-0.01", "10000000000", "0.001" }) {
      BigDecimal amount = new BigDecimal(invalid);
      assertThrows(
        IllegalArgumentException.class,
        () -> new PlanBudget(amount, BigDecimal.TEN, null)
      );
      assertThrows(
        IllegalArgumentException.class,
        () -> new PlanBudget(BigDecimal.ZERO, amount, null)
      );
    }
  }

  @Test
  void reversedRangeIsRejected() {
    assertThrows(
      IllegalArgumentException.class,
      () -> new PlanBudget(BigDecimal.TEN, BigDecimal.ONE, null)
    );
  }

  @Test
  void oversizedNotesAreRejectedEvenWithoutAnEstimate() {
    assertThrows(IllegalArgumentException.class, () -> new PlanBudget(null, null, "a".repeat(501)));
  }

  @Test
  void partiallyHydratedNullableColumnsDoNotExposeAnEstimate() throws ReflectiveOperationException {
    PlanBudget budget = new PlanBudget();
    Field minimum = PlanBudget.class.getDeclaredField("minimum");
    minimum.setAccessible(true);
    minimum.set(budget, BigDecimal.ONE);
    assertFalse(budget.isEstimated());
    assertFalse(budget.isRange());
  }
}
