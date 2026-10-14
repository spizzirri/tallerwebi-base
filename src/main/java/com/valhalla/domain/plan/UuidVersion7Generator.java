package com.valhalla.domain.plan;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.function.LongSupplier;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.uuid.UuidValueGenerator;

/**
 * Generador de UUID v7 (RFC 9562) para {@link Plan#id}, replicando lo que
 * {@code @UuidGenerator(style = VERSION_7)} trae de fábrica en Hibernate 7: en 6.6 el estilo
 * VERSION_7 es incubating, pero el hook {@code algorithm =} ya existe, así que esta clase enchufa
 * el mismo esquema (timestamp de 48 bits + versión 7 + variante RFC + contador monotónico).
 *
 * <p>Al migrar a Spring 7 + Hibernate 7 esta clase se borra y la anotación en {@link Plan} pasa a
 * {@code @UuidGenerator(style = VERSION_7)}: es un cambio de una línea.
 */
public class UuidVersion7Generator implements UuidValueGenerator {

  private static final SecureRandom RANDOM = new SecureRandom();
  private static final long COUNTER_MASK = 0x3FFF_FFFF_FFFF_FFFFL;
  private static final int RAND_A_SHIFT = 50;

  private final LongSupplier clock;
  private long lastMillis;
  private long lastSequence;

  public UuidVersion7Generator() {
    this(System::currentTimeMillis);
  }

  UuidVersion7Generator(LongSupplier clock) {
    this.clock = clock;
  }

  @Override
  public synchronized UUID generateUuid(SharedSessionContractImplementor session) {
    long now = clock.getAsLong();
    if (now > lastMillis) {
      // Milisegundo nuevo (o reloj atrasado hacia adelante): semilla fresca.
      lastMillis = now;
      lastSequence = RANDOM.nextLong() & COUNTER_MASK;
    } else {
      // Mismo milisegundo o reloj atrasado: incrementa el contador para no repetir ids.
      lastSequence = (lastSequence + 1) & COUNTER_MASK;
    }
    long msb = (lastMillis << 16) | 0x7000L | ((lastSequence >>> RAND_A_SHIFT) & 0x0FFFL);
    long lsb = 0x8000_0000_0000_0000L | lastSequence;
    return new UUID(msb, lsb);
  }
}
