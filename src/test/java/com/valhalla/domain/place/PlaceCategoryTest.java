package com.valhalla.domain.place;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Optional;
import org.junit.jupiter.api.Test;

public class PlaceCategoryTest {

  @Test
  public void shouldResolveAKnownCode() {
    Optional<PlaceCategory> result = PlaceCategory.fromCode("CAFE");

    assertThat(result.orElseThrow(), is(equalTo(PlaceCategory.CAFE)));
  }

  @Test
  public void shouldReturnEmptyForAnUnknownCode() {
    assertThat(PlaceCategory.fromCode("NO_SUCH_CATEGORY").isEmpty(), is(true));
  }

  @Test
  public void shouldReturnEmptyForBlankOrNullCode() {
    assertThat(PlaceCategory.fromCode("  ").isEmpty(), is(true));
    assertThat(PlaceCategory.fromCode(null).isEmpty(), is(true));
  }

  @Test
  public void shouldMapRestaurantAmenities() {
    assertThat(
      PlaceCategory.fromOsm("amenity", "restaurant"),
      is(equalTo(PlaceCategory.RESTAURANT))
    );
    assertThat(
      PlaceCategory.fromOsm("amenity", "fast_food"),
      is(equalTo(PlaceCategory.RESTAURANT))
    );
  }

  @Test
  public void shouldMapBarPubNightclubAndMuseumAmenities() {
    assertThat(PlaceCategory.fromOsm("amenity", "bar"), is(equalTo(PlaceCategory.BAR)));
    assertThat(PlaceCategory.fromOsm("amenity", "pub"), is(equalTo(PlaceCategory.BAR)));
    assertThat(PlaceCategory.fromOsm("amenity", "nightclub"), is(equalTo(PlaceCategory.NIGHTLIFE)));
    assertThat(PlaceCategory.fromOsm("amenity", "museum"), is(equalTo(PlaceCategory.MUSEUM)));
  }

  @Test
  public void shouldMapAnyShopValueToShopping() {
    assertThat(PlaceCategory.fromOsm("shop", "supermarket"), is(equalTo(PlaceCategory.SHOPPING)));
    assertThat(PlaceCategory.fromOsm("shop", "bakery"), is(equalTo(PlaceCategory.SHOPPING)));
    assertThat(
      PlaceCategory.fromOsm("shop", "anything_at_all"),
      is(equalTo(PlaceCategory.SHOPPING))
    );
  }

  @Test
  public void shouldMapUnmappedTagsToOther() {
    assertThat(
      PlaceCategory.fromOsm("amenity", "vending_machine"),
      is(equalTo(PlaceCategory.OTHER))
    );
    assertThat(PlaceCategory.fromOsm("tourism", "hotel"), is(equalTo(PlaceCategory.OTHER)));
  }

  @Test
  public void shouldMapNullOrBlankTagsToOther() {
    assertThat(PlaceCategory.fromOsm(null, "restaurant"), is(equalTo(PlaceCategory.OTHER)));
    assertThat(PlaceCategory.fromOsm("amenity", null), is(equalTo(PlaceCategory.OTHER)));
    assertThat(PlaceCategory.fromOsm("", "restaurant"), is(equalTo(PlaceCategory.OTHER)));
    assertThat(PlaceCategory.fromOsm("amenity", "   "), is(equalTo(PlaceCategory.OTHER)));
  }

  @Test
  public void shouldMapRemainingAmenityFamilies() {
    assertThat(PlaceCategory.fromOsm("amenity", "cafe"), is(equalTo(PlaceCategory.CAFE)));
    assertThat(PlaceCategory.fromOsm("amenity", "theatre"), is(equalTo(PlaceCategory.CULTURE)));
    assertThat(PlaceCategory.fromOsm("amenity", "cinema"), is(equalTo(PlaceCategory.CULTURE)));
    assertThat(PlaceCategory.fromOsm("amenity", "arts_centre"), is(equalTo(PlaceCategory.CULTURE)));
    assertThat(PlaceCategory.fromOsm("amenity", "library"), is(equalTo(PlaceCategory.CULTURE)));
    assertThat(PlaceCategory.fromOsm("amenity", "planetarium"), is(equalTo(PlaceCategory.CULTURE)));
    assertThat(PlaceCategory.fromOsm("amenity", "park"), is(equalTo(PlaceCategory.PARK)));
    assertThat(PlaceCategory.fromOsm("amenity", "garden"), is(equalTo(PlaceCategory.PARK)));
    assertThat(PlaceCategory.fromOsm("amenity", "playground"), is(equalTo(PlaceCategory.PARK)));
    assertThat(
      PlaceCategory.fromOsm("amenity", "fitness_centre"),
      is(equalTo(PlaceCategory.SPORT))
    );
    assertThat(PlaceCategory.fromOsm("amenity", "sport_centre"), is(equalTo(PlaceCategory.SPORT)));
  }

  @Test
  public void shouldNormalizeCaseAndWhitespaceVariants() {
    assertThat(
      PlaceCategory.fromOsm("Amenity", "Restaurant"),
      is(equalTo(PlaceCategory.RESTAURANT))
    );
    assertThat(PlaceCategory.fromOsm(" amenity ", " bar "), is(equalTo(PlaceCategory.BAR)));
    assertThat(PlaceCategory.fromOsm("SHOP", "kiosk"), is(equalTo(PlaceCategory.SHOPPING)));
  }
}
