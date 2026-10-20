package com.valhalla.domain.place;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository {
  List<Place> findAll();

  Optional<Place> findById(Long id);

  List<Place> findByCategory(PlaceCategory category);

  List<Place> findByNameContainingIgnoreCase(String name);

  List<Place> findByCategoryAndNameContainingIgnoreCase(PlaceCategory category, String name);

  void save(Place place);

  void deleteById(Long id);

  long count();
}
