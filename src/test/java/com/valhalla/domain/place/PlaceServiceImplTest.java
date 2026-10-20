package com.valhalla.domain.place;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.valhalla.domain.exception.PlaceInUseException;
import com.valhalla.domain.exception.PlaceNotFoundException;
import com.valhalla.domain.planplace.PlanPlaceRepository;
import com.valhalla.infrastructure.place.PlaceServiceImpl;
import com.valhalla.presentation.place.EditPlaceRequest;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PlaceServiceImplTest {

  private PlaceRepository placeRepositoryMock;
  private PlanPlaceRepository planPlaceRepositoryMock;
  private PlaceService placeService;

  @BeforeEach
  public void init() {
    placeRepositoryMock = mock(PlaceRepository.class);
    planPlaceRepositoryMock = mock(PlanPlaceRepository.class);
    placeService = new PlaceServiceImpl(placeRepositoryMock, planPlaceRepositoryMock);
  }

  @Test
  public void shouldReturnAllPlaces() {
    List<Place> places = List.of(place("Cafe Tortoni", PlaceCategory.CAFE));
    when(placeRepositoryMock.findAll()).thenReturn(places);

    List<Place> result = placeService.getAllPlaces();

    assertThat(result, is(sameInstance(places)));
    verify(placeRepositoryMock).findAll();
  }

  @Test
  public void shouldReturnPlacesForCategory() {
    List<Place> places = List.of(place("Cafe Tortoni", PlaceCategory.CAFE));
    when(placeRepositoryMock.findByCategory(PlaceCategory.CAFE)).thenReturn(places);

    List<Place> result = placeService.getPlacesByCategory(PlaceCategory.CAFE);

    assertThat(result, is(sameInstance(places)));
    verify(placeRepositoryMock).findByCategory(PlaceCategory.CAFE);
  }

  @Test
  public void shouldSearchPlacesByName() {
    List<Place> places = List.of(place("Parrilla Don Julio", PlaceCategory.RESTAURANT));
    when(placeRepositoryMock.findByNameContainingIgnoreCase("Don")).thenReturn(places);

    List<Place> result = placeService.searchPlaces("Don");

    assertThat(result, is(sameInstance(places)));
    verify(placeRepositoryMock).findByNameContainingIgnoreCase("Don");
  }

  @Test
  public void shouldSearchPlacesByNameWithinCategory() {
    List<Place> places = List.of(place("Parrilla Don Julio", PlaceCategory.RESTAURANT));
    when(
      placeRepositoryMock.findByCategoryAndNameContainingIgnoreCase(PlaceCategory.RESTAURANT, "Don")
    )
      .thenReturn(places);

    List<Place> result = placeService.searchPlaces("Don", PlaceCategory.RESTAURANT);

    assertThat(result, is(sameInstance(places)));
    verify(placeRepositoryMock)
      .findByCategoryAndNameContainingIgnoreCase(PlaceCategory.RESTAURANT, "Don");
  }

  @Test
  public void shouldReturnPlaceWhenIdExists() {
    Place expectedPlace = place("MALBA", PlaceCategory.MUSEUM);
    expectedPlace.setId(1L);
    when(placeRepositoryMock.findById(1L)).thenReturn(Optional.of(expectedPlace));

    Optional<Place> result = placeService.getPlaceById(1L);

    assertThat(result.orElseThrow(), is(equalTo(expectedPlace)));
    verify(placeRepositoryMock).findById(1L);
  }

  @Test
  public void shouldReturnEmptyWhenIdDoesNotExist() {
    when(placeRepositoryMock.findById(99L)).thenReturn(Optional.empty());

    Optional<Place> result = placeService.getPlaceById(99L);

    assertThat(result.isEmpty(), is(true));
    verify(placeRepositoryMock).findById(99L);
  }

  @Test
  public void shouldCreateAPlaceMappingEveryFieldAndSavingBlankImageAsNull() {
    EditPlaceRequest request = new EditPlaceRequest(
      "El Banco Rojo",
      PlaceCategory.RESTAURANT,
      "Cafe de especialidad",
      "Av. Rivadavia 456",
      "Mo-Su 08:00-20:00",
      "+54 11 5555-0001",
      "https://elbankrojo.test",
      "   ",
      -34.6091,
      -58.3917
    );

    Place result = placeService.create(request);

    assertThat(result.getName(), is(equalTo("El Banco Rojo")));
    assertThat(result.getCategory(), is(equalTo(PlaceCategory.RESTAURANT)));
    assertThat(result.getDescription(), is(equalTo("Cafe de especialidad")));
    assertThat(result.getAddress(), is(equalTo("Av. Rivadavia 456")));
    assertThat(result.getImageUrl(), is(nullValue()));
    assertThat(result.getLatitude(), is(equalTo(-34.6091)));
    assertThat(result.getLongitude(), is(equalTo(-58.3917)));
    assertThat(result.getOpeningHours(), is(equalTo("Mo-Su 08:00-20:00")));
    assertThat(result.getPhone(), is(equalTo("+54 11 5555-0001")));
    assertThat(result.getWebsite(), is(equalTo("https://elbankrojo.test")));
    verify(placeRepositoryMock).save(result);
  }

  @Test
  public void shouldUpdateAnExistingPlaceApplyingTheSameMapping() {
    Place existing = place("Old Name", PlaceCategory.OTHER);
    existing.setId(7L);
    when(placeRepositoryMock.findById(7L)).thenReturn(Optional.of(existing));
    EditPlaceRequest request = new EditPlaceRequest(
      "New Name",
      PlaceCategory.MUSEUM,
      "Updated description",
      "Cerrito 123",
      "Tu-Su 10:00-18:00",
      "+54 11 5555-0002",
      "https://museo.test",
      "https://example.test/img.png",
      -34.589,
      -58.397
    );

    Place result = placeService.update(7L, request);

    assertThat(result, is(sameInstance(existing)));
    assertThat(result.getName(), is(equalTo("New Name")));
    assertThat(result.getCategory(), is(equalTo(PlaceCategory.MUSEUM)));
    assertThat(result.getDescription(), is(equalTo("Updated description")));
    assertThat(result.getAddress(), is(equalTo("Cerrito 123")));
    assertThat(result.getImageUrl(), is(equalTo("https://example.test/img.png")));
    assertThat(result.getLatitude(), is(equalTo(-34.589)));
    assertThat(result.getLongitude(), is(equalTo(-58.397)));
    assertThat(result.getOpeningHours(), is(equalTo("Tu-Su 10:00-18:00")));
    assertThat(result.getPhone(), is(equalTo("+54 11 5555-0002")));
    assertThat(result.getWebsite(), is(equalTo("https://museo.test")));
    verify(placeRepositoryMock).save(existing);
  }

  @Test
  public void shouldThrowPlaceNotFoundWhenUpdatingAMissingId() {
    when(placeRepositoryMock.findById(99L)).thenReturn(Optional.empty());
    EditPlaceRequest request = new EditPlaceRequest(
      "Any",
      PlaceCategory.BAR,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null
    );

    assertThrows(PlaceNotFoundException.class, () -> placeService.update(99L, request));
  }

  @Test
  public void shouldDeleteAPlaceThatIsNotReferencedByAnyPlan() {
    when(planPlaceRepositoryMock.existsByPlaceId(5L)).thenReturn(false);

    placeService.delete(5L);

    verify(planPlaceRepositoryMock).existsByPlaceId(5L);
    verify(placeRepositoryMock).deleteById(5L);
  }

  @Test
  public void shouldRefuseToDeleteAPlaceReferencedByAPlanWithoutTouchingTheRow() {
    when(planPlaceRepositoryMock.existsByPlaceId(5L)).thenReturn(true);

    assertThrows(PlaceInUseException.class, () -> placeService.delete(5L));

    verify(planPlaceRepositoryMock).existsByPlaceId(5L);
    verify(placeRepositoryMock, never()).deleteById(5L);
  }

  private Place place(String name, PlaceCategory category) {
    Place place = new Place();
    place.setName(name);
    place.setCategory(category);
    return place;
  }
}
