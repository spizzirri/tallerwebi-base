package com.valhalla.domain.bookmark;

import com.valhalla.domain.place.Place;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.UuidVersion7Generator;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

/**
 * [MARK] Un plan publico que un usuario guardo en su lista privada de "Guardados".
 *
 * <p>Guarda un <strong>snapshot</strong> del encabezado y del itinerario en el momento de guardar.
 * {@code planId} es una columna plana, <strong>sin FK</strong>: si el autor borra el plan, la fila
 * sobrevive y la card sigue visible como "Eliminado" (y sigue siendo clonable). El snapshot es el
 * fallback: mientras el plan vivo exista y sea accesible, la UI y el clon lo prefieren a el.
 *
 * <p>Es privado del usuario que lo guardo. {@link Plan} no tiene la coleccion inversa: los
 * guardados se leen solo desde el perfil propio (ver 20-MARK.md).
 */
@Entity
@Table(
  name = "plan_bookmarks",
  uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "plan_id" })
)
public class PlanBookmark {

  @Id
  @UuidGenerator(algorithm = UuidVersion7Generator.class)
  private UUID id;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "plan_id", nullable = false)
  private UUID planId;

  @Embedded
  private PlanBookmarkHeader header = new PlanBookmarkHeader();

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @OneToMany(
    mappedBy = "bookmark",
    cascade = CascadeType.ALL,
    orphanRemoval = true,
    fetch = FetchType.LAZY
  )
  @OrderBy("sortOrder ASC")
  private List<PlanBookmarkPlace> places = new ArrayList<>();

  public PlanBookmark() {}

  /**
   * Copia el plan a un snapshot nuevo para {@code user}. El llamador ya valido que el plan sea
   * publico: aca no hay reglas de visibilidad, solo la copia de los campos.
   */
  public static PlanBookmark snapshotOf(Plan plan, User user) {
    PlanBookmark bookmark = new PlanBookmark();
    bookmark.user = user;
    bookmark.planId = plan.getId();
    bookmark.header.setName(plan.getName());
    bookmark.header.setDescription(plan.getDescription());
    bookmark.header.setEventDate(plan.getEventDate());
    bookmark.header.setEventTime(plan.getEventTime());
    bookmark.header.setPlaceCount(plan.getPlanPlaces().size());
    User author = plan.getAdministrator();
    if (author != null) {
      bookmark.header.setAdministratorId(author.getId());
      bookmark.header.setAdministratorName(displayNameOf(author));
    }
    int order = 1;
    for (PlanPlace entry : plan.getPlanPlaces()) {
      bookmark.addPlace(snapshotOf(entry, order));
      order++;
    }
    return bookmark;
  }

  private static PlanBookmarkPlace snapshotOf(PlanPlace entry, int order) {
    Place place = entry.getPlace();
    PlanBookmarkPlace row = new PlanBookmarkPlace();
    row.setPlaceId(place.getId());
    row.setPlaceName(place.getName());
    row.setPlaceCategory(place.getCategory());
    row.setPlaceAddress(place.getAddress());
    row.setDescription(entry.getDescription());
    row.setVisitDate(entry.getVisitDate());
    row.setVisitTime(entry.getVisitTime());
    row.setSortOrder(order);
    return row;
  }

  /** Nombre para mostrar, con el email como ultimo recurso cuando no hay nombre cargado. */
  public static String displayNameOf(User user) {
    String name =
      ((user.getFirstName() == null ? "" : user.getFirstName()) +
        " " +
        (user.getLastName() == null ? "" : user.getLastName())).trim();
    return name.isEmpty() ? user.getEmail() : name;
  }

  public void addPlace(PlanBookmarkPlace place) {
    places.add(place);
    place.setBookmark(this);
  }

  public UUID getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public UUID getPlanId() {
    return planId;
  }

  public PlanBookmarkHeader getHeader() {
    return header;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public List<PlanBookmarkPlace> getPlaces() {
    return places;
  }
}
