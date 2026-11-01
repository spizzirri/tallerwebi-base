package com.valhalla.infrastructure.planplace;

import com.valhalla.domain.exception.PlanBannedException;
import com.valhalla.domain.exception.PlanNotFoundException;
import com.valhalla.domain.place.PlaceRepository;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.plan.PlanScheduler;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.planplace.PlanPlaceRepository;
import com.valhalla.domain.planplace.PlanPlaceService;
import com.valhalla.domain.planplace.PlanPlaceUpdate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
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
  public PlanPlace addPlaceToPlan(UUID planId, UUID placeId, String ownerEmail) {
    Plan plan = planService.getEditablePlan(planId, ownerEmail);
    PlanPlace entry = new PlanPlace();
    plan.addPlanPlace(entry);
    entry.setPlace(places.findById(placeId).orElseThrow(PlanNotFoundException::new));
    entry.setSortOrder(
      repository.findByPlanId(planId).stream().mapToInt(PlanPlace::getSortOrder).max().orElse(0) + 1
    );
    // [SCH] La parada nueva entra en el orden del itinerario y el horario se recalcula entero: su
    // traslado depende de la parada anterior y la cadena de las siguientes arranca desde ella.
    PlanScheduler.apply(plan);
    return repository.save(entry);
  }

  @Transactional(readOnly = true)
  @Override
  public List<PlanPlace> getItinerary(UUID planId, String ownerEmail) {
    Plan plan = plans.findById(planId).orElseThrow(PlanNotFoundException::new);
    if (plan.isBanned(ownerEmail)) {
      throw new PlanBannedException();
    }
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
    UUID planId,
    UUID id,
    PlanPlaceUpdate changes,
    String ownerEmail
  ) {
    PlanPlace entry = ownedEntry(planId, id, ownerEmail);
    entry.setDurationMinutes(changes.durationMinutes());
    entry.setTransportModeToNext(changes.transportModeToNext());
    // [SCH] Nulo no es "dejar como esta": es volver al calculo por distancia y modo.
    entry.setTravelMinutesToNext(changes.travelMinutesToNext());
    entry.setVisitDate(changes.visitDate());
    entry.setVisitTime(changes.visitTime());
    // [SCH] Una fecha u hora enviada es un pin aunque llegue una sola de las dos: el usuario eligio
    // ese dato y la derivacion tiene que arrancar de ahi en vez de pisarlo.
    entry.setTimePinned(changes.visitDate() != null || changes.visitTime() != null);
    // [SCH] La parada editada puede correr a las siguientes: se deriva el plan completo, no la fila.
    PlanScheduler.apply(entry.getPlan());
    entry.getPlace().getName();
    return repository.save(entry);
  }

  @Override
  public void removePlaceFromPlan(UUID planId, UUID id, String ownerEmail) {
    PlanPlace entry = ownedEntry(planId, id, ownerEmail);
    Plan plan = entry.getPlan();
    plan.getPlanPlaces().remove(entry);
    // [SCH] Sacar una parada corre las siguientes: el hueco que deja su visita y su traslado se
    // cierra en la misma pasada, antes de que la fila desaparezca.
    PlanScheduler.apply(plan);
    repository.deleteById(id);
  }

  @Override
  public void reorderPlaces(UUID planId, List<UUID> entryIds, String ownerEmail) {
    Plan plan = planService.getEditablePlan(planId, ownerEmail);
    List<PlanPlace> entries = repository.findByPlanId(planId);
    if (!matches(entryIds, entries)) {
      throw new IllegalArgumentException("Provide each itinerary entry exactly once");
    }
    reorder(entries, entryIds);
    // [SCH] El planificador recorre la coleccion del plan, que conserva el orden con el que se
    // cargo: hay que reordenarla en memoria antes de derivar, o el horario nuevo seguiria al viejo.
    plan.getPlanPlaces().sort(Comparator.comparing(PlanPlace::getSortOrder));
    // [SCH] Con el recalculo automatico apagado el usuario conserva las horas que fijo: el nuevo
    // orden mueve la posicion de las paradas, no su horario.
    if (plan.getSettings().getAutoSchedule()) {
      PlanScheduler.apply(plan);
    }
  }

  private boolean matches(List<UUID> entryIds, List<PlanPlace> entries) {
    if (entryIds == null || entryIds.size() != entries.size()) {
      return false;
    }
    java.util.Set<UUID> expected = new java.util.HashSet<>(entryIds);
    java.util.Set<UUID> actual = new java.util.HashSet<>();
    for (PlanPlace entry : entries) {
      actual.add(entry.getId());
    }
    return expected.equals(actual);
  }

  private void reorder(List<PlanPlace> entries, List<UUID> entryIds) {
    for (int i = 0; i < entryIds.size(); i++) {
      UUID id = entryIds.get(i);
      for (PlanPlace entry : entries) {
        if (entry.getId().equals(id)) {
          entry.setSortOrder(i + 1);
          repository.save(entry);
          break;
        }
      }
    }
  }

  @Transactional(readOnly = true)
  @Override
  public boolean isPlaceInPlan(UUID planId, UUID placeId) {
    return repository.existsByPlanIdAndPlaceId(planId, placeId);
  }

  private PlanPlace ownedEntry(UUID planId, UUID id, String ownerEmail) {
    planService.getEditablePlan(planId, ownerEmail);
    PlanPlace entry = repository.findById(id).orElseThrow(PlanNotFoundException::new);
    if (!planId.equals(entry.getPlan().getId())) {
      throw new PlanNotFoundException();
    }
    return entry;
  }
}
