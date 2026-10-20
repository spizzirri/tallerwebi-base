package com.valhalla.domain.place;

import com.valhalla.presentation.place.EditPlaceRequest;
import java.util.List;
import java.util.Optional;

public interface PlaceService {
  List<Place> getAllPlaces();

  Optional<Place> getPlaceById(Long id);

  List<Place> getPlacesByCategory(PlaceCategory category);

  List<Place> searchPlaces(String query);

  List<Place> searchPlaces(String query, PlaceCategory category);

  Place create(EditPlaceRequest request);

  Place update(Long id, EditPlaceRequest request);

  void delete(Long id);
}
