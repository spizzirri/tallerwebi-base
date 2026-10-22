package com.valhalla.domain.place;

import com.valhalla.presentation.place.EditPlaceRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlaceService {
  List<Place> getAllPlaces();

  Optional<Place> getPlaceById(UUID id);

  List<Place> getPlacesByCategory(PlaceCategory category);

  List<Place> searchPlaces(String query);

  List<Place> searchPlaces(String query, PlaceCategory category);

  Place create(EditPlaceRequest request);

  Place update(UUID id, EditPlaceRequest request);

  void delete(UUID id);
}
