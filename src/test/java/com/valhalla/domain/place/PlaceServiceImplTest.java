package com.valhalla.domain.place;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.valhalla.domain.exception.PlaceInUseException;
import com.valhalla.domain.exception.PlaceNotFoundException;
import com.valhalla.domain.planplace.PlanPlaceRepository;
import com.valhalla.infrastructure.place.PlaceServiceImpl;
import com.valhalla.presentation.place.EditPlaceRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PlaceServiceImplTest {

  private static final UUID PLACE_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000001");
  private static final UUID OTHER_PLACE_ID = UUID.fromString(
    "0192f0a1-0000-7000-8000-000000000007"
  );
  private static final UUID MISSING_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000099");
  private static final UUID PLACE_TO_DELETE = UUID.fromString(
    "0192f0a1-0000-7000-8000-000000000005"
  );

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
    expectedPlace.setId(PLACE_ID);
    when(placeRepositoryMock.findById(PLACE_ID)).thenReturn(Optional.of(expectedPlace));

    Optional<Place> result = placeService.getPlaceById(PLACE_ID);

    assertThat(result.orElseThrow(), is(equalTo(expectedPlace)));
    verify(placeRepositoryMock).findById(PLACE_ID);
  }

  @Test
  public void shouldReturnEmptyWhenIdDoesNotExist() {
    when(placeRepositoryMock.findById(MISSING_ID)).thenReturn(Optional.empty());

    Optional<Place> result = placeService.getPlaceById(MISSING_ID);

    assertThat(result.isEmpty(), is(true));
    verify(placeRepositoryMock).findById(MISSING_ID);
  }

  @Test
  public void shouldCreateAPlaceMappingEveryFieldAndSavingBlankImageAsNull() {
    EditPlaceRequest request = new EditPlaceRequest();
    request.setName("El Banco Rojo");
    request.setCategory(PlaceCategory.RESTAURANT);
    request.setDescription("Cafe de especialidad");
    request.setAddress("Av. Rivadavia 456");
    request.setOpeningHours("Mo-Su 08:00-20:00");
    request.setPhone("+54 11 5555-0001");
    request.setWebsite("https://elbankrojo.test");
    request.setImageUrl("   ");
    request.setLatitude(-34.6091);
    request.setLongitude(-58.3917);
    request.setDefaultDurationMinutes(45);

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
    assertThat(result.getDefaultDurationMinutes(), is(equalTo(45)));
    verify(placeRepositoryMock).save(result);
  }

  @Test
  public void shouldUpdateAnExistingPlaceApplyingTheSameMapping() {
    Place existing = place("Old Name", PlaceCategory.OTHER);
    existing.setId(OTHER_PLACE_ID);
    existing.setDefaultDurationMinutes(120);
    when(placeRepositoryMock.findById(OTHER_PLACE_ID)).thenReturn(Optional.of(existing));
    EditPlaceRequest request = new EditPlaceRequest();
    request.setName("New Name");
    request.setCategory(PlaceCategory.MUSEUM);
    request.setDescription("Updated description");
    request.setAddress("Cerrito 123");
    request.setOpeningHours("Tu-Su 10:00-18:00");
    request.setPhone("+54 11 5555-0002");
    request.setWebsite("https://museo.test");
    request.setImageUrl("https://example.test/img.png");
    request.setLatitude(-34.589);
    request.setLongitude(-58.397);

    Place result = placeService.update(OTHER_PLACE_ID, request);

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
    assertThat(
      "una edicion sin duracion tipica vuelve a 'segun categoria'",
      result.getDefaultDurationMinutes(),
      is(nullValue())
    );
    verify(placeRepositoryMock).save(existing);
  }

  @Test
  public void shouldUpdateTheExistingPlaceWhenTheExternalIdIsAlreadyStored() {
    Place existing = place("Nombre Viejo", PlaceCategory.CAFE);
    existing.setExternalId("osm:node/1");
    when(placeRepositoryMock.findByExternalId("osm:node/1")).thenReturn(Optional.of(existing));

    Place result = placeService.create(requestWith("Nombre Nuevo", "osm:node/1"));

    assertThat("el alta reusa la fila del POI", result, is(sameInstance(existing)));
    assertThat(result.getName(), is(equalTo("Nombre Nuevo")));
    assertThat(result.getExternalId(), is(equalTo("osm:node/1")));
    verify(placeRepositoryMock).save(existing);
  }

  @Test
  public void shouldCreateANewPlaceWhenTheExternalIdIsUnknown() {
    when(placeRepositoryMock.findByExternalId("osm:node/2")).thenReturn(Optional.empty());

    Place result = placeService.create(requestWith("Lugar Importado", "osm:node/2"));

    assertThat(result.getExternalId(), is(equalTo("osm:node/2")));
    assertThat(result.getName(), is(equalTo("Lugar Importado")));
    verify(placeRepositoryMock).save(result);
  }

  @Test
  public void shouldNotLookUpByIdentityWhenTheFormBringsNoExternalId() {
    Place result = placeService.create(requestWith("Lugar Manual", null));

    verify(placeRepositoryMock, never()).findByExternalId(any());
    assertThat(result.getExternalId(), is(nullValue()));
    verify(placeRepositoryMock).save(result);
  }

  @Test
  public void shouldKeepTheExternalIdWhenAnUpdateDoesNotBringOne() {
    Place existing = place("Importado", PlaceCategory.BAR);
    existing.setId(OTHER_PLACE_ID);
    existing.setExternalId("osm:node/3");
    when(placeRepositoryMock.findById(OTHER_PLACE_ID)).thenReturn(Optional.of(existing));

    Place result = placeService.update(OTHER_PLACE_ID, requestWith("Renombrado", null));

    assertThat(result.getName(), is(equalTo("Renombrado")));
    assertThat(
      "editar sin identidad no borra la del POI",
      result.getExternalId(),
      is("osm:node/3")
    );
    verify(placeRepositoryMock).save(existing);
  }

  @Test
  public void shouldThrowPlaceNotFoundWhenUpdatingAMissingId() {
    when(placeRepositoryMock.findById(MISSING_ID)).thenReturn(Optional.empty());
    EditPlaceRequest request = new EditPlaceRequest();
    request.setName("Any");
    request.setCategory(PlaceCategory.BAR);

    assertThrows(PlaceNotFoundException.class, () -> placeService.update(MISSING_ID, request));
  }

  @Test
  public void shouldReturnNoSavedPlaceWhenTheListIsEmpty() {
    Map<String, UUID> saved = placeService.savedPlaceIds(List.of());

    assertThat(saved.isEmpty(), is(true));
    verify(placeRepositoryMock, never()).findByExternalIdIn(any());
  }

  @Test
  public void shouldIgnoreBlankIdentities() {
    Map<String, UUID> saved = placeService.savedPlaceIds(Arrays.asList("", "  ", null));

    assertThat(saved.isEmpty(), is(true));
    verify(placeRepositoryMock, never()).findByExternalIdIn(any());
  }

  @Test
  public void shouldMapTheSavedPlacesByIdentity() {
    Place first = place("Cafe Tortoni", PlaceCategory.CAFE);
    first.setId(PLACE_ID);
    first.setExternalId("osm:node/1");
    Place second = place("Parrilla Don Julio", PlaceCategory.RESTAURANT);
    second.setId(OTHER_PLACE_ID);
    second.setExternalId("osm:way/2");
    when(placeRepositoryMock.findByExternalIdIn(List.of("osm:node/1", "osm:way/2", "osm:node/404")))
      .thenReturn(List.of(first, second));

    Map<String, UUID> saved = placeService.savedPlaceIds(
      List.of("osm:node/1", "osm:way/2", "osm:node/404")
    );

    assertThat(saved.size(), is(2));
    assertThat(saved.get("osm:node/1"), is(PLACE_ID));
    assertThat(saved.get("osm:way/2"), is(OTHER_PLACE_ID));
    assertThat(
      "una identidad sin lugar no aparece en el mapa",
      saved.containsKey("osm:node/404"),
      is(false)
    );
  }

  @Test
  public void shouldDeleteAPlaceThatIsNotReferencedByAnyPlan() {
    when(planPlaceRepositoryMock.existsByPlaceId(PLACE_TO_DELETE)).thenReturn(false);

    placeService.delete(PLACE_TO_DELETE);

    verify(planPlaceRepositoryMock).existsByPlaceId(PLACE_TO_DELETE);
    verify(placeRepositoryMock).deleteById(PLACE_TO_DELETE);
  }

  @Test
  public void shouldRefuseToDeleteAPlaceReferencedByAPlanWithoutTouchingTheRow() {
    when(planPlaceRepositoryMock.existsByPlaceId(PLACE_TO_DELETE)).thenReturn(true);

    assertThrows(PlaceInUseException.class, () -> placeService.delete(PLACE_TO_DELETE));

    verify(planPlaceRepositoryMock).existsByPlaceId(PLACE_TO_DELETE);
    verify(placeRepositoryMock, never()).deleteById(PLACE_TO_DELETE);
  }

  private Place place(String name, PlaceCategory category) {
    Place place = new Place();
    place.setName(name);
    place.setCategory(category);
    return place;
  }

  /** Alta o edicion minima: el constructor de 10 argumentos no lleva la identidad de origen. */
  private EditPlaceRequest requestWith(String name, String externalId) {
    EditPlaceRequest request = new EditPlaceRequest();
    request.setName(name);
    request.setCategory(PlaceCategory.CAFE);
    request.setExternalId(externalId);
    return request;
  }
}
