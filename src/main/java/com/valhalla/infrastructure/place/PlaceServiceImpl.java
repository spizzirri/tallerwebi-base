package com.valhalla.infrastructure.place;

import com.valhalla.domain.exception.PlaceInUseException;
import com.valhalla.domain.exception.PlaceNotFoundException;
import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceCategory;
import com.valhalla.domain.place.PlaceRepository;
import com.valhalla.domain.place.PlaceService;
import com.valhalla.domain.planplace.PlanPlaceRepository;
import com.valhalla.presentation.place.EditPlaceRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PlaceServiceImpl implements PlaceService {

  private final PlaceRepository placeRepository;
  private final PlanPlaceRepository planPlaceRepository;

  @Autowired
  public PlaceServiceImpl(
    PlaceRepository placeRepository,
    PlanPlaceRepository planPlaceRepository
  ) {
    this.placeRepository = placeRepository;
    this.planPlaceRepository = planPlaceRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Place> getAllPlaces() {
    return placeRepository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Place> getPlaceById(Long id) {
    return placeRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Place> getPlacesByCategory(PlaceCategory category) {
    return placeRepository.findByCategory(category);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Place> searchPlaces(String query) {
    return placeRepository.findByNameContainingIgnoreCase(query);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Place> searchPlaces(String query, PlaceCategory category) {
    return placeRepository.findByCategoryAndNameContainingIgnoreCase(category, query);
  }

  @Override
  public Place create(EditPlaceRequest request) {
    Place place = new Place();
    applyRequest(place, request);
    placeRepository.save(place);
    return place;
  }

  @Override
  public Place update(Long id, EditPlaceRequest request) {
    Place place = placeRepository.findById(id).orElseThrow(PlaceNotFoundException::new);
    applyRequest(place, request);
    placeRepository.save(place);
    return place;
  }

  /**
   * Baja protegida: la FK {@code plan_places.place_id} no tiene cascada, así que borrar un lugar
   * referenciado por planes revienta contra la restricción. Se chequea antes de tocar la fila y,
   * si está en uso, se niega sin borrar nada (AC-11). Un id inexistente no se chequea aparte:
   * {@code deleteById} de Spring Data es un no-op silencioso sobre una fila ausente.
   */
  @Override
  public void delete(Long id) {
    if (planPlaceRepository.existsByPlaceId(id)) {
      throw new PlaceInUseException();
    }
    placeRepository.deleteById(id);
  }

  private void applyRequest(Place place, EditPlaceRequest request) {
    place.setName(request.getName());
    place.setDescription(request.getDescription());
    place.setCategory(request.getCategory());
    place.setAddress(request.getAddress());
    place.setImageUrl(blankToNull(request.getImageUrl()));
    place.setLatitude(request.getLatitude());
    place.setLongitude(request.getLongitude());
    place.setOpeningHours(request.getOpeningHours());
    place.setPhone(request.getPhone());
    place.setWebsite(request.getWebsite());
  }

  private String blankToNull(String value) {
    return (value == null || value.isBlank()) ? null : value;
  }
}
