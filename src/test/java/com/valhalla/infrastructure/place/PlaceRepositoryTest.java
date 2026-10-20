package com.valhalla.infrastructure.place;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceCategory;
import com.valhalla.domain.place.PlaceRepository;
import com.valhalla.integration.JpaIntegrationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@JpaIntegrationTest
@Transactional
public class PlaceRepositoryTest {

  @Autowired
  private PlaceRepository placeRepository;

  @Test
  public void shouldFindSeededPlaces() {
    List<Place> places = placeRepository.findAll();

    assertThat(places.size(), is(equalTo(12)));
  }

  @Test
  public void shouldFindOnlyPlacesInRequestedCategory() {
    List<Place> places = placeRepository.findByCategory(PlaceCategory.RESTAURANT);

    assertThat(places.size(), is(greaterThanOrEqualTo(1)));
    assertThat(places, everyItem(notNullValue()));
    assertThat(
      places.stream().allMatch(place -> PlaceCategory.RESTAURANT == place.getCategory()),
      is(true)
    );
  }

  @Test
  public void shouldFindPlacesWithPartialCaseInsensitiveName() {
    List<Place> places = placeRepository.findByNameContainingIgnoreCase("don");

    assertThat(places.size(), is(equalTo(1)));
    assertThat(places.getFirst().getName(), is(equalTo("Parrilla Don Julio")));
  }

  @Test
  public void shouldFindPlacesMatchingCategoryAndPartialCaseInsensitiveName() {
    List<Place> places = placeRepository.findByCategoryAndNameContainingIgnoreCase(
      PlaceCategory.RESTAURANT,
      "don"
    );

    assertThat(places.size(), is(equalTo(1)));
    assertThat(places.getFirst().getName(), is(equalTo("Parrilla Don Julio")));
  }
}
