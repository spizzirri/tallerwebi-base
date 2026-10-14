package com.valhalla.domain.plan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class UuidVersion7GeneratorTest {

  private static final long FIXED_MILLIS = 1_700_000_000_000L;

  @Test
  void generaVersion7YVarianteRfc() {
    UuidVersion7Generator generator = new UuidVersion7Generator(() -> FIXED_MILLIS);

    UUID uuid = generator.generateUuid(null);

    assertEquals(7, uuid.version());
    // Variante RFC 9562: bits altos "10" -> variant() == 2.
    assertEquals(2, uuid.variant());
  }

  @Test
  void mismoMilisegundoProduceIdsMonotonicos() {
    UuidVersion7Generator generator = new UuidVersion7Generator(() -> FIXED_MILLIS);

    UUID first = generator.generateUuid(null);
    UUID second = generator.generateUuid(null);

    assertTrue(second.compareTo(first) > 0, "el segundo id del mismo ms debe ser mayor");
    assertEquals(7, second.version());
    assertEquals(2, second.variant());
  }

  @Test
  void relojAvanzadoFijaElTimestampDe48Bits() {
    AtomicLong clock = new AtomicLong(FIXED_MILLIS);
    UuidVersion7Generator generator = new UuidVersion7Generator(clock::get);

    UUID first = generator.generateUuid(null);
    clock.set(FIXED_MILLIS + 1);
    UUID second = generator.generateUuid(null);

    assertEquals(FIXED_MILLIS, first.getMostSignificantBits() >>> 16);
    assertEquals(FIXED_MILLIS + 1, second.getMostSignificantBits() >>> 16);
    assertTrue(second.compareTo(first) > 0, "un reloj adelantado debe producir un id mayor");
  }

  @Test
  void constructorSinArgumentosGeneraUuidSano() {
    UuidVersion7Generator generator = new UuidVersion7Generator();

    UUID first = generator.generateUuid(null);
    UUID second = generator.generateUuid(null);

    assertEquals(7, first.version());
    assertEquals(2, first.variant());
    assertNotEquals(first, second);
  }
}
