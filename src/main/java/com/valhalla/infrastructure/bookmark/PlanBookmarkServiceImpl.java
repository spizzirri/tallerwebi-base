package com.valhalla.infrastructure.bookmark;

import com.valhalla.domain.bookmark.BookmarkEntry;
import com.valhalla.domain.bookmark.BookmarkStatus;
import com.valhalla.domain.bookmark.PlanBookmark;
import com.valhalla.domain.bookmark.PlanBookmarkHeader;
import com.valhalla.domain.bookmark.PlanBookmarkPlace;
import com.valhalla.domain.bookmark.PlanBookmarkRepository;
import com.valhalla.domain.bookmark.PlanBookmarkService;
import com.valhalla.domain.exception.PlanNotFoundException;
import com.valhalla.domain.exception.UserNotFoundException;
import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceRepository;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lives in infrastructure, not domain: the domain package must not depend on the framework. */
@Service
@Transactional
public class PlanBookmarkServiceImpl implements PlanBookmarkService {

  private final PlanBookmarkRepository planBookmarkRepository;
  private final PlanRepository planRepository;
  private final UserRepository userRepository;
  private final PlaceRepository placeRepository;
  private final PlanService planService;

  @Autowired
  public PlanBookmarkServiceImpl(
    PlanBookmarkRepository planBookmarkRepository,
    PlanRepository planRepository,
    UserRepository userRepository,
    PlaceRepository placeRepository,
    PlanService planService
  ) {
    this.planBookmarkRepository = planBookmarkRepository;
    this.planRepository = planRepository;
    this.userRepository = userRepository;
    this.placeRepository = placeRepository;
    this.planService = planService;
  }

  @Override
  public void save(UUID planId, String userEmail) {
    User user = userRepository.findByEmail(userEmail).orElseThrow(UserNotFoundException::new);
    Plan plan = planRepository.findById(planId).orElseThrow(PlanNotFoundException::new);
    if (!plan.getIsPublic()) {
      // Privado e inexistente responden igual: guardar no confirma que el id exista.
      throw new PlanNotFoundException();
    }
    boolean alreadySaved = planBookmarkRepository
      .findByUserIdAndPlanId(user.getId(), planId)
      .isPresent();
    if (alreadySaved) {
      return;
    }
    planBookmarkRepository.save(PlanBookmark.snapshotOf(plan, user));
  }

  @Override
  public void remove(UUID planId, String userEmail) {
    User user = userRepository.findByEmail(userEmail).orElseThrow(UserNotFoundException::new);
    planBookmarkRepository.deleteByUserIdAndPlanId(user.getId(), planId);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isBookmarked(UUID planId, String userEmail) {
    if (userEmail == null || userEmail.isBlank()) {
      return false;
    }
    return userRepository
      .findByEmail(userEmail)
      .map(user -> planBookmarkRepository.findByUserIdAndPlanId(user.getId(), planId).isPresent())
      .orElse(false);
  }

  @Override
  @Transactional(readOnly = true)
  public Set<UUID> getBookmarkedPlanIds(String userEmail) {
    if (userEmail == null || userEmail.isBlank()) {
      return Set.of();
    }
    return userRepository
      .findByEmail(userEmail)
      .map(user -> planBookmarkRepository.findPlanIdsByUserId(user.getId()))
      .orElse(Set.of());
  }

  @Override
  @Transactional(readOnly = true)
  public List<BookmarkEntry> getBookmarks(String userEmail) {
    return userRepository
      .findByEmail(userEmail)
      .map(user ->
        planBookmarkRepository
          .findByUserId(user.getId())
          .stream()
          .map(bookmark -> toEntry(bookmark, userEmail))
          .toList()
      )
      .orElse(List.of());
  }

  /**
   * El plan vivo gana mientras siga accesible: asi la card refleja las ediciones del autor. Si el
   * usuario perdio el acceso, cae al snapshot y la card muestra "Privado".
   */
  private BookmarkEntry toEntry(PlanBookmark bookmark, String userEmail) {
    Optional<Plan> live = planRepository.findById(bookmark.getPlanId());
    if (live.isEmpty()) {
      return fromSnapshot(bookmark, BookmarkStatus.DELETED);
    }
    Plan plan = live.get();
    if (!isAccessible(plan, userEmail)) {
      return fromSnapshot(bookmark, BookmarkStatus.PRIVATE);
    }
    User author = plan.getAdministrator();
    return new BookmarkEntry(
      bookmark.getId(),
      plan.getId(),
      plan.getName(),
      plan.getDescription(),
      plan.getEventDate(),
      plan.getEventTime(),
      plan.getPlanPlaces().size(),
      author == null ? null : author.getId(),
      author == null ? null : PlanBookmark.displayNameOf(author),
      BookmarkStatus.AVAILABLE,
      plan.getIsPublic()
    );
  }

  private static BookmarkEntry fromSnapshot(PlanBookmark bookmark, BookmarkStatus status) {
    PlanBookmarkHeader header = bookmark.getHeader();
    return new BookmarkEntry(
      bookmark.getId(),
      bookmark.getPlanId(),
      header.getName(),
      header.getDescription(),
      header.getEventDate(),
      header.getEventTime(),
      header.getPlaceCount(),
      header.getAdministratorId(),
      header.getAdministratorName(),
      status,
      false
    );
  }

  @Override
  public Plan cloneBookmark(UUID planId, String userEmail) {
    User cloner = userRepository.findByEmail(userEmail).orElseThrow(UserNotFoundException::new);
    PlanBookmark bookmark = planBookmarkRepository
      .findByUserIdAndPlanId(cloner.getId(), planId)
      .orElseThrow(PlanNotFoundException::new);
    Optional<Plan> live = planRepository.findById(planId);
    if (live.isPresent() && isAccessible(live.get(), userEmail)) {
      // [CLO] El plan sigue disponible: se reusa su clon (incluye participantes y ediciones).
      return planService.clonePlan(planId, userEmail);
    }
    return cloneFromSnapshot(bookmark, userEmail);
  }

  /**
   * Reconstruye un plan privado propio desde el snapshot. Las paradas cuyo lugar ya no existe en el
   * catalogo se omiten: el plan se puede clonar igual, sin las paradas huerfanas.
   */
  private Plan cloneFromSnapshot(PlanBookmark bookmark, String userEmail) {
    PlanBookmarkHeader header = bookmark.getHeader();
    Plan copy = new Plan();
    copy.setName(header.getName());
    copy.setDescription(header.getDescription());
    copy.setEventDate(header.getEventDate());
    copy.setEventTime(header.getEventTime());
    // La copia nace privada: publicarla es decision del nuevo dueno (misma regla que [CLO]).
    copy.setIsPublic(false);
    int order = 1;
    for (PlanBookmarkPlace row : bookmark.getPlaces()) {
      Optional<Place> place = findExistingPlace(row);
      if (place.isEmpty()) {
        continue;
      }
      PlanPlace entry = new PlanPlace();
      entry.setPlace(place.get());
      entry.setDescription(row.getDescription());
      entry.setVisitDate(row.getVisitDate());
      entry.setVisitTime(row.getVisitTime());
      entry.setSortOrder(order);
      order++;
      copy.addPlanPlace(entry);
    }
    return planService.createPlan(copy, userEmail);
  }

  private Optional<Place> findExistingPlace(PlanBookmarkPlace row) {
    if (row.getPlaceId() == null) {
      return Optional.empty();
    }
    return placeRepository.findById(row.getPlaceId());
  }

  private static boolean isAccessible(Plan plan, String userEmail) {
    return (plan.getIsPublic() || plan.isAdministrator(userEmail) || plan.isParticipant(userEmail));
  }
}
