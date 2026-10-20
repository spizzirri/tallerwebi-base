package com.valhalla.infrastructure.place;

import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceCategory;
import com.valhalla.domain.place.PlaceRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PlaceRepositoryImpl implements PlaceRepository {

  private final JpaPlaceRepository jpaPlaceRepository;

  @Autowired
  public PlaceRepositoryImpl(JpaPlaceRepository jpaPlaceRepository) {
    this.jpaPlaceRepository = jpaPlaceRepository;
  }

  @Override
  public List<Place> findAll() {
    return jpaPlaceRepository.findAll();
  }

  @Override
  public Optional<Place> findById(Long id) {
    return jpaPlaceRepository.findById(id);
  }

  @Override
  public List<Place> findByCategory(PlaceCategory category) {
    return jpaPlaceRepository.findByCategory(category);
  }

  @Override
  public List<Place> findByNameContainingIgnoreCase(String name) {
    return jpaPlaceRepository.findByNameContainingIgnoreCase(name);
  }

  @Override
  public List<Place> findByCategoryAndNameContainingIgnoreCase(
    PlaceCategory category,
    String name
  ) {
    return jpaPlaceRepository.findByCategoryAndNameContainingIgnoreCase(category, name);
  }

  @Override
  public void save(Place place) {
    jpaPlaceRepository.save(place);
  }

  @Override
  public void deleteById(Long id) {
    jpaPlaceRepository.deleteById(id);
  }

  @Override
  public long count() {
    return jpaPlaceRepository.count();
  }
}
