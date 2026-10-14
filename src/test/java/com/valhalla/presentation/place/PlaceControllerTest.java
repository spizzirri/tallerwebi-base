package com.valhalla.presentation.place;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceCategory;
import com.valhalla.domain.place.PlaceService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class PlaceControllerTest {

  private PlaceController controller;
  private PlaceService placeServiceMock;

  @BeforeEach
  public void init() {
    placeServiceMock = mock(PlaceService.class);
    controller = new PlaceController(placeServiceMock);
  }

  @Test
  public void shouldReturnAllPlacesWhenListHasNoFilters() {
    List<Place> places = List.of(place(1L, "MALBA", PlaceCategory.MUSEUM));
    when(placeServiceMock.getAllPlaces()).thenReturn(places);

    ModelAndView result = controller.listPlaces(null, null);

    assertThat(result.getViewName(), is(equalTo("pages/places/list")));
    assertThat(result.getModel().get("places"), is(sameInstance(places)));
    verify(placeServiceMock).getAllPlaces();
  }

  @Test
  public void shouldReturnCategoryFilteredPlaces() {
    List<Place> places = List.of(place(1L, "Cafe Tortoni", PlaceCategory.CAFE));
    when(placeServiceMock.getPlacesByCategory(PlaceCategory.CAFE)).thenReturn(places);

    ModelAndView result = controller.listPlaces("CAFE", null);

    assertThat(result.getModel().get("places"), is(sameInstance(places)));
    verify(placeServiceMock).getPlacesByCategory(PlaceCategory.CAFE);
  }

  @Test
  public void shouldApplySearchWithinSelectedCategory() {
    List<Place> places = List.of(place(1L, "Parrilla Don Julio", PlaceCategory.RESTAURANT));
    when(placeServiceMock.searchPlaces("Don", PlaceCategory.RESTAURANT)).thenReturn(places);

    ModelAndView result = controller.listPlaces("RESTAURANT", "Don");

    assertThat(result.getModel().get("places"), is(sameInstance(places)));
    verify(placeServiceMock).searchPlaces("Don", PlaceCategory.RESTAURANT);
  }

  @Test
  public void shouldReturnNameFilteredPlaces() {
    List<Place> places = List.of(place(1L, "Parrilla Don Julio", PlaceCategory.RESTAURANT));
    when(placeServiceMock.searchPlaces("Don")).thenReturn(places);

    ModelAndView result = controller.listPlaces(null, "Don");

    assertThat(result.getModel().get("places"), is(sameInstance(places)));
    verify(placeServiceMock).searchPlaces("Don");
  }

  @Test
  public void shouldReturnNoPlacesForUnknownCategory() {
    ModelAndView result = controller.listPlaces("UNKNOWN", "Don");

    assertThat(result.getModel().get("places"), is(equalTo(List.of())));
    verifyNoInteractions(placeServiceMock);
  }

  private Place place(Long id, String name, PlaceCategory category) {
    Place place = new Place();
    place.setId(id);
    place.setName(name);
    place.setCategory(category);
    return place;
  }
}
