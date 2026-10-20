package com.valhalla.domain.place;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class BoundingBoxTest {

  private static final BoundingBox PALERMO = new BoundingBox(-34.588, -58.436, -34.57, -58.412);

  @Test
  public void shouldExposeTheCabaBoxInOverpassOrder() {
    assertThat(BoundingBox.CABA.toOverpass(), is("-34.705800,-58.531500,-34.526600,-58.335100"));
  }

  @Test
  public void shouldRejectALatitudeBelowTheMinimum() {
    assertThrows(IllegalArgumentException.class, () -> new BoundingBox(-91, 0, -34, 1));
    assertThrows(IllegalArgumentException.class, () -> new BoundingBox(-34, 0, 91, 1));
  }

  @Test
  public void shouldRejectALongitudeOutOfRange() {
    assertThrows(IllegalArgumentException.class, () -> new BoundingBox(-34, -181, -33, -58));
    assertThrows(IllegalArgumentException.class, () -> new BoundingBox(-34, -58, -33, 181));
  }

  /** NaN entra sin quejarse por comparacion; el rango lo tiene que frenar. */
  @Test
  public void shouldRejectANonNumericCoordinate() {
    assertThrows(
      IllegalArgumentException.class,
      () -> new BoundingBox(Double.NaN, -58, -34, -58.3)
    );
  }

  @Test
  public void shouldRejectAnInvertedBox() {
    assertThrows(IllegalArgumentException.class, () -> new BoundingBox(-34, -58.4, -35, -58.3));
    assertThrows(IllegalArgumentException.class, () -> new BoundingBox(-34, -58.3, -33, -58.4));
  }

  @Test
  public void shouldClipABoxThatSticksOutOfCaba() {
    BoundingBox wider = new BoundingBox(-35, -59, -34, -58);

    assertThat(wider.intersection(BoundingBox.CABA), is(BoundingBox.CABA));
  }

  @Test
  public void shouldReturnNullWhenTheBoxesDoNotTouch() {
    // Al norte de CABA: el recorte queda al reves en latitud.
    BoundingBox farNorth = new BoundingBox(10, -58.4, 20, -58.3);
    // Misma latitud que CABA pero al este: solo se cruza el recorte de longitud.
    BoundingBox farEast = new BoundingBox(-34.6, -57, -34.5, -56);

    assertThat(farNorth.intersection(BoundingBox.CABA), is(nullValue()));
    assertThat(farEast.intersection(BoundingBox.CABA), is(nullValue()));
  }

  @Test
  public void shouldRoundTheCacheKeyToAHundredMeters() {
    BoundingBox shifted = new BoundingBox(-34.58804, -58.43604, -34.57004, -58.41204);

    assertThat(PALERMO.cacheKey(), is(shifted.cacheKey()));
  }
}
