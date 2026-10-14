package com.valhalla.infrastructure.planplace;

import com.valhalla.domain.exception.PlanNotFoundException;
import com.valhalla.domain.place.PlaceRepository;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.planplace.PlanPlaceRepository;
import com.valhalla.domain.planplace.PlanPlaceService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// The proposal places this Spring service in domain; existing services and component scanning use infrastructure.
@Service
@Transactional
public class PlanPlaceServiceImpl implements PlanPlaceService {

  private final PlanPlaceRepository repository;
  private final PlanRepository plans;
  private final PlanService planService;
  private final PlaceRepository places;

  public PlanPlaceServiceImpl(
    PlanPlaceRepository repository,
    PlanRepository plans,
    PlanService planService,
    PlaceRepository places
  ) {
    this.repository = repository;
    this.plans = plans;
    this.planService = planService;
    this.places = places;
  }

  @Override
  public PlanPlace addPlaceToPlan(Long planId, Long placeId, String ownerEmail) {
    Plan plan = planService.getEditablePlan(planId, ownerEmail);
    PlanPlace entry = new PlanPlace();
    plan.addPlanPlace(entry);
    entry.setPlace(places.findById(placeId).orElseThrow(PlanNotFoundException::new));
    entry.setSortOrder(
      repository.findByPlanId(planId).stream().mapToInt(PlanPlace::getSortOrder).max().orElse(0) + 1
    );
    return repository.save(entry);
  }

  @Transactional(readOnly = true)
  @Override
  public List<PlanPlace> getItinerary(Long planId, String ownerEmail) {
    Plan plan = plans.findById(planId).orElseThrow(PlanNotFoundException::new);
    if (!plan.getIsPublic()) {
      if (ownerEmail == null) {
        throw new PlanNotFoundException();
      }
      if (!plan.isParticipant(ownerEmail)) {
        planService.getEditablePlan(planId, ownerEmail);
      }
    }
    List<PlanPlace> itinerary = repository.findByPlanId(planId);
    itinerary.forEach(entry -> entry.getPlace().getName());
    return itinerary;
  }

  @Override
  public PlanPlace updatePlanPlace(
    Long planId,
    Long id,
    LocalDate date,
    LocalTime time,
    String ownerEmail
  ) {
    PlanPlace entry = ownedEntry(planId, id, ownerEmail);
    entry.setVisitDate(date);
    entry.setVisitTime(time);
    entry.getPlace().getName();
    return repository.save(entry);
  }

  @Override
  public void removePlaceFromPlan(Long planId, Long id, String ownerEmail) {
    PlanPlace entry = ownedEntry(planId, id, ownerEmail);
    entry.getPlan().getPlanPlaces().remove(entry);
    repository.deleteById(id);
  }

  @Override
  public void reorderPlaces(Long planId, List<Long> entryIds, String ownerEmail) {
    planService.getEditablePlan(planId, ownerEmail);
    List<PlanPlace> entries = repository.findByPlanId(planId);
    // The same place can appear several times, so entries are identified by their own id.
    if (
      entryIds == null ||
      entryIds.size() != entries.size() ||
      !new HashSet<>(entryIds)
        .equals(new HashSet<>(entries.stream().map(PlanPlace::getId).toList()))
    ) {
      throw new IllegalArgumentException("Provide each itinerary entry exactly once");
    }
    for (PlanPlace entry : entries) {
      entry.setSortOrder(entryIds.indexOf(entry.getId()) + 1);
      repository.save(entry);
    }
  }

  @Transactional(readOnly = true)
  @Override
  public boolean isPlaceInPlan(Long planId, Long placeId) {
    return repository.existsByPlanIdAndPlaceId(planId, placeId);
  }

  private PlanPlace ownedEntry(Long planId, Long id, String ownerEmail) {
    planService.getEditablePlan(planId, ownerEmail);
    PlanPlace entry = repository.findById(id).orElseThrow(PlanNotFoundException::new);
    if (!planId.equals(entry.getPlan().getId())) {
      throw new PlanNotFoundException();
    }
    return entry;
  }
}
