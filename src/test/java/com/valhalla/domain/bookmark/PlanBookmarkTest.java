package com.valhalla.domain.bookmark;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceCategory;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.user.User;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** [MARK] Unit tests of the bookmark aggregate: snapshot copy and display name. */
public class PlanBookmarkTest {

  private static final UUID PLAN_ID = UUID.fromString("0192f0a1-0000-7000-8000-00000000000a");
  private static final UUID PLACE_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000005");
  private static final UUID AUTHOR_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000063");
  private static final UUID BOOKMARK_ID = UUID.fromString("0192f0a1-0000-7000-8000-0000000000b1");

  @Test
  public void T_MARK_entity_snapshotOf_copiaEncabezadoEItinerario() {
    Plan plan = new Plan();
    plan.setId(PLAN_ID);
    plan.setName("Plan de prueba");
    plan.setDescription("Descripcion");
    plan.setEventDate(LocalDate.of(2026, 11, 7));
    plan.setEventTime(LocalTime.of(18, 30));
    plan.setAdministrator(user("autor@test.com", "Ada", "Lovelace"));
    plan.addPlanPlace(entry(1));

    PlanBookmark bookmark = PlanBookmark.snapshotOf(
      plan,
      user("duenio@test.com", "Grace", "Hopper")
    );

    assertThat(bookmark.getUser().getEmail(), is("duenio@test.com"));
    assertThat(bookmark.getPlanId(), is(PLAN_ID));
    assertThat(bookmark.getCreatedAt(), is(notNullValue()));
    assertThat(bookmark.getHeader().getName(), is("Plan de prueba"));
    assertThat(bookmark.getHeader().getDescription(), is("Descripcion"));
    assertThat(bookmark.getHeader().getEventDate(), is(LocalDate.of(2026, 11, 7)));
    assertThat(bookmark.getHeader().getEventTime(), is(LocalTime.of(18, 30)));
    assertThat(bookmark.getHeader().getPlaceCount(), is(1));
    assertThat(bookmark.getHeader().getAdministratorId(), is(AUTHOR_ID));
    assertThat(bookmark.getHeader().getAdministratorName(), is("Ada Lovelace"));

    PlanBookmarkPlace row = bookmark.getPlaces().get(0);
    assertThat(row.getId(), is((Long) null));
    assertThat(row.getBookmark(), is(bookmark));
    assertThat(row.getPlaceId(), is(PLACE_ID));
    assertThat(row.getPlaceName(), is("Cafe"));
    assertThat(row.getPlaceCategory(), is(PlaceCategory.CAFE));
    assertThat(row.getPlaceAddress(), is("Calle 1"));
    assertThat(row.getDescription(), is("Primera parada"));
    assertThat(row.getVisitDate(), is(LocalDate.of(2026, 11, 7)));
    assertThat(row.getVisitTime(), is(LocalTime.of(9, 0)));
    assertThat(row.getSortOrder(), is(1));
  }

  @Test
  public void T_MARK_entity_snapshotOf_sinAutorNiParadas() {
    Plan plan = new Plan();
    plan.setId(PLAN_ID);
    plan.setName("Vacio");

    PlanBookmark bookmark = PlanBookmark.snapshotOf(plan, user("duenio@test.com", null, null));

    assertThat(bookmark.getHeader().getPlaceCount(), is(0));
    assertThat(bookmark.getHeader().getAdministratorId(), is((UUID) null));
    assertThat(bookmark.getHeader().getAdministratorName(), is((String) null));
    assertThat(bookmark.getPlaces().isEmpty(), is(true));
  }

  @Test
  public void T_MARK_entity_displayNameOf_caeAlEmailCuandoNoHayNombre() {
    assertThat(PlanBookmark.displayNameOf(user("solo@test.com", null, null)), is("solo@test.com"));
    assertThat(PlanBookmark.displayNameOf(user("solo@test.com", "Ada", null)), is("Ada"));
    assertThat(PlanBookmark.displayNameOf(user("solo@test.com", null, "Lovelace")), is("Lovelace"));
    assertThat(
      PlanBookmark.displayNameOf(user("solo@test.com", "Ada", "Lovelace")),
      is("Ada Lovelace")
    );
  }

  @Test
  public void T_MARK_entity_bookmarkEntry_openableSoloCuandoEstaAvailable() {
    BookmarkEntry available = entry(BookmarkStatus.AVAILABLE);
    assertThat(available.openable(), is(true));
    assertThat(entry(BookmarkStatus.PRIVATE).openable(), is(false));
    assertThat(entry(BookmarkStatus.DELETED).openable(), is(false));
    assertThat(BookmarkStatus.values().length, is(3));
  }

  private static BookmarkEntry entry(BookmarkStatus status) {
    return new BookmarkEntry(
      BOOKMARK_ID,
      PLAN_ID,
      "Plan",
      null,
      null,
      null,
      0,
      null,
      null,
      status,
      false
    );
  }

  private static PlanPlace entry(int sortOrder) {
    Place place = new Place();
    place.setId(PLACE_ID);
    place.setName("Cafe");
    place.setCategory(PlaceCategory.CAFE);
    place.setAddress("Calle 1");
    PlanPlace entry = new PlanPlace();
    entry.setPlace(place);
    entry.setDescription("Primera parada");
    entry.setVisitDate(LocalDate.of(2026, 11, 7));
    entry.setVisitTime(LocalTime.of(9, 0));
    entry.setSortOrder(sortOrder);
    return entry;
  }

  private static User user(String email, String firstName, String lastName) {
    User user = new User();
    user.setId(AUTHOR_ID);
    user.setEmail(email);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    return user;
  }
}
