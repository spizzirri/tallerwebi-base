package com.valhalla.infrastructure;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceCategory;
import com.valhalla.domain.place.PlaceRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Unit tests of {@link PlaceDataSeeder} against a mocked repository (U-05..U-09, AC-07/AC-02).
 *
 * <p>El catalogo es fijo: 12 lugares (los 10 originales intactos + Obelisco y Monumental, que
 * cubren OTHER y SPORT) y ninguno setea imageUrl, asi que /explore cae al placeholder por
 * defecto.
 */
public class PlaceDataSeederTest {

  private static final int SEEDED_TOTAL = 12;

  /** Los 10 lugares originales: nombre -> categoria, tal como estaban antes de 12-DATA. */
  private static final Map<String, PlaceCategory> ORIGINAL_PLACES = Map.ofEntries(
    Map.entry("El Sanjuanino", PlaceCategory.RESTAURANT),
    Map.entry("La Viruta", PlaceCategory.NIGHTLIFE),
    Map.entry("MALBA", PlaceCategory.MUSEUM),
    Map.entry("Plaza Serrano", PlaceCategory.SHOPPING),
    Map.entry("Cafe Tortoni", PlaceCategory.CAFE),
    Map.entry("Parque Tres de Febrero", PlaceCategory.PARK),
    Map.entry("El Ateneo Grand Splendid", PlaceCategory.CULTURE),
    Map.entry("Cerveceria General San Martin", PlaceCategory.BAR),
    Map.entry("Planetario Galileo Galilei", PlaceCategory.CULTURE),
    Map.entry("Parrilla Don Julio", PlaceCategory.RESTAURANT)
  );

  private PlaceRepository placeRepositoryMock;
  private PlaceDataSeeder seeder;

  @BeforeEach
  public void init() {
    this.placeRepositoryMock = mock(PlaceRepository.class);
    this.seeder = new PlaceDataSeeder(this.placeRepositoryMock);
    when(this.placeRepositoryMock.count()).thenReturn(0L);
  }

  @Test
  public void elCatalogoEstaVacioNoSiembraNada() {
    // given: la base ya tiene lugares (corrida previa)
    when(this.placeRepositoryMock.count()).thenReturn(5L);

    // when
    this.seeder.ensureSeeded();

    // then
    verify(this.placeRepositoryMock, never()).save(any(Place.class));
  }

  @Test
  public void siembraElDatasetCompletoDeDoceLugares() {
    // when
    this.seeder.ensureSeeded();

    // then
    verify(this.placeRepositoryMock, times(SEEDED_TOTAL)).save(any(Place.class));
    List<Place> saved = savedPlaces();
    assertThat(saved, hasSize(SEEDED_TOTAL));
    assertThat(saved, everyItem(notNullValue()));
  }

  @Test
  public void cadaCategoriaTieneAlMenosUnLugar() {
    // when
    this.seeder.ensureSeeded();

    // then: las 10 categorias de PlaceCategory aparecen al menos una vez
    List<PlaceCategory> categories = savedPlaces().stream().map(Place::getCategory).toList();
    for (PlaceCategory category : PlaceCategory.values()) {
      assertThat(categories.contains(category), is(true));
    }
  }

  @Test
  public void losDiezLugaresOriginalesSiguenConSuNombreYCategoria() {
    // when
    this.seeder.ensureSeeded();

    // then
    Map<String, Place> byName = savedPlaces()
      .stream()
      .collect(Collectors.toMap(Place::getName, Function.identity()));
    // U-08: los 10 originales siguen presentes (subset, no el set completo: el catalogo tiene 12)
    for (Map.Entry<String, PlaceCategory> original : ORIGINAL_PLACES.entrySet()) {
      Place place = byName.get(original.getKey());
      assertThat("falta " + original.getKey(), place, is(notNullValue()));
      assertThat(place.getCategory(), is(equalTo(original.getValue())));
    }
  }

  @Test
  public void ningunLugarDelCatalogoSeteaImageUrl() {
    // when
    this.seeder.ensureSeeded();

    // then: todos null, el placeholder es el default (U-09)
    for (Place place : savedPlaces()) {
      assertThat(place.getImageUrl(), is(nullValue()));
    }
  }

  private List<Place> savedPlaces() {
    ArgumentCaptor<Place> captor = ArgumentCaptor.forClass(Place.class);
    verify(this.placeRepositoryMock, times(SEEDED_TOTAL)).save(captor.capture());
    return captor.getAllValues();
  }
}
