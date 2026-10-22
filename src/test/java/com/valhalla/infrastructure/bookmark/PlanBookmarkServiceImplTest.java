package com.valhalla.infrastructure.bookmark;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.valhalla.domain.bookmark.BookmarkEntry;
import com.valhalla.domain.bookmark.BookmarkStatus;
import com.valhalla.domain.bookmark.PlanBookmark;
import com.valhalla.domain.bookmark.PlanBookmarkHeader;
import com.valhalla.domain.bookmark.PlanBookmarkPlace;
import com.valhalla.domain.bookmark.PlanBookmarkRepository;
import com.valhalla.domain.exception.PlanNotFoundException;
import com.valhalla.domain.exception.UserNotFoundException;
import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceCategory;
import com.valhalla.domain.place.PlaceRepository;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** [MARK] Unit tests of {@link PlanBookmarkServiceImpl} against mocked ports (20-MARK.md). */
public class PlanBookmarkServiceImplTest {

  private static final String EMAIL = "guardador@test.com";
  private static final UUID USER_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000015");
  private static final UUID AUTHOR_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000063");
  private static final UUID PLAN_ID = UUID.fromString("0192f0a1-0000-7000-8000-00000000000a");
  private static final UUID OTHER_PLAN_ID = UUID.fromString("0192f0a1-0000-7000-8000-00000000000b");
  private static final UUID PLACE_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000005");
  private static final UUID GONE_PLACE_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000006");

  private PlanBookmarkServiceImpl bookmarkService;
  private PlanBookmarkRepository planBookmarkRepositoryMock;
  private PlanRepository planRepositoryMock;
  private UserRepository userRepositoryMock;
  private PlaceRepository placeRepositoryMock;
  private PlanService planServiceMock;

  @BeforeEach
  public void init() {
    this.planBookmarkRepositoryMock = mock(PlanBookmarkRepository.class);
    this.planRepositoryMock = mock(PlanRepository.class);
    this.userRepositoryMock = mock(UserRepository.class);
    this.placeRepositoryMock = mock(PlaceRepository.class);
    this.planServiceMock = mock(PlanService.class);
    this.bookmarkService =
      new PlanBookmarkServiceImpl(
        this.planBookmarkRepositoryMock,
        this.planRepositoryMock,
        this.userRepositoryMock,
        this.placeRepositoryMock,
        this.planServiceMock
      );
  }

  // --- save: camino feliz ---

  @Test
  public void T_MARK_001_save_guardaUnPlanPublicoConElUsuarioDeLaSesion() {
    when(this.userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.of(user(USER_ID, EMAIL)));
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(publicPlan(PLAN_ID)));
    when(this.planBookmarkRepositoryMock.findByUserIdAndPlanId(USER_ID, PLAN_ID))
      .thenReturn(Optional.empty());

    this.bookmarkService.save(PLAN_ID, EMAIL);

    ArgumentCaptor<PlanBookmark> captor = ArgumentCaptor.forClass(PlanBookmark.class);
    verify(this.planBookmarkRepositoryMock).save(captor.capture());
    assertThat(captor.getValue().getUser().getEmail(), is(EMAIL));
    assertThat(captor.getValue().getPlanId(), is(PLAN_ID));
  }

  @Test
  public void T_MARK_002_save_copiaElEncabezadoDelPlanAlSnapshot() {
    Plan plan = publicPlan(PLAN_ID);
    plan.setDescription("Un dia completo");
    plan.setEventDate(LocalDate.of(2026, 11, 7));
    plan.setEventTime(LocalTime.of(18, 30));
    plan.setAdministrator(user(AUTHOR_ID, "autor@test.com", "Ada", "Lovelace"));
    when(this.userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.of(user(USER_ID, EMAIL)));
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(plan));
    when(this.planBookmarkRepositoryMock.findByUserIdAndPlanId(USER_ID, PLAN_ID))
      .thenReturn(Optional.empty());

    this.bookmarkService.save(PLAN_ID, EMAIL);

    PlanBookmarkHeader header = capturedBookmark().getHeader();
    assertThat(header.getName(), is(plan.getName()));
    assertThat(header.getDescription(), is("Un dia completo"));
    assertThat(header.getEventDate(), is(LocalDate.of(2026, 11, 7)));
    assertThat(header.getEventTime(), is(LocalTime.of(18, 30)));
    assertThat(header.getPlaceCount(), is(1));
    assertThat(header.getAdministratorId(), is(AUTHOR_ID));
    assertThat(header.getAdministratorName(), is("Ada Lovelace"));
  }

  @Test
  public void T_MARK_003_save_copiaElItinerarioCompleto() {
    Plan plan = publicPlan(PLAN_ID);
    plan.getPlanPlaces().clear();
    Place place = place(PLACE_ID, "Cafe");
    plan.addPlanPlace(
      entry(place, "Primera parada", LocalDate.of(2026, 11, 7), LocalTime.of(9, 0), 1)
    );
    when(this.userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.of(user(USER_ID, EMAIL)));
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(plan));
    when(this.planBookmarkRepositoryMock.findByUserIdAndPlanId(USER_ID, PLAN_ID))
      .thenReturn(Optional.empty());

    this.bookmarkService.save(PLAN_ID, EMAIL);

    PlanBookmarkPlace row = capturedBookmark().getPlaces().get(0);
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
  public void T_MARK_004_save_dosVecesNoDuplicaLaFila() {
    PlanBookmark existing = snapshot(publicPlan(PLAN_ID), user(USER_ID, EMAIL));
    when(this.userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.of(user(USER_ID, EMAIL)));
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(publicPlan(PLAN_ID)));
    when(this.planBookmarkRepositoryMock.findByUserIdAndPlanId(USER_ID, PLAN_ID))
      .thenReturn(Optional.of(existing));

    this.bookmarkService.save(PLAN_ID, EMAIL);

    verify(this.planBookmarkRepositoryMock, never()).save(any(PlanBookmark.class));
  }

  @Test
  public void T_MARK_005_save_rechazaUnPlanPrivadoComoInexistente() {
    Plan privatePlan = publicPlan(PLAN_ID);
    privatePlan.setIsPublic(false);
    when(this.userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.of(user(USER_ID, EMAIL)));
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(privatePlan));

    assertThrows(PlanNotFoundException.class, () -> this.bookmarkService.save(PLAN_ID, EMAIL));
    verify(this.planBookmarkRepositoryMock, never()).save(any(PlanBookmark.class));
  }

  @Test
  public void T_MARK_006_save_rechazaUnIdInexistente() {
    when(this.userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.of(user(USER_ID, EMAIL)));
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.empty());

    assertThrows(PlanNotFoundException.class, () -> this.bookmarkService.save(PLAN_ID, EMAIL));
    verify(this.planBookmarkRepositoryMock, never()).save(any(PlanBookmark.class));
  }

  @Test
  public void T_MARK_006b_save_fallaSiElUsuarioNoExiste() {
    when(this.userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> this.bookmarkService.save(PLAN_ID, EMAIL));
  }

  // --- remove ---

  @Test
  public void T_MARK_007_remove_borraLaFilaDelParUsuarioPlan() {
    when(this.userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.of(user(USER_ID, EMAIL)));

    this.bookmarkService.remove(PLAN_ID, EMAIL);

    verify(this.planBookmarkRepositoryMock).deleteByUserIdAndPlanId(USER_ID, PLAN_ID);
  }

  @Test
  public void T_MARK_008_remove_deUnPlanQueNoEstabaGuardadoNoLanza() {
    when(this.userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.of(user(USER_ID, EMAIL)));

    this.bookmarkService.remove(OTHER_PLAN_ID, EMAIL);

    verify(this.planBookmarkRepositoryMock).deleteByUserIdAndPlanId(USER_ID, OTHER_PLAN_ID);
  }

  @Test
  public void T_MARK_008b_remove_fallaSiElUsuarioNoExiste() {
    when(this.userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> this.bookmarkService.remove(PLAN_ID, EMAIL));
  }

  // --- getBookmarks: orden, privacidad y estados ---

  @Test
  public void T_MARK_009_getBookmarks_devuelveLosGuardadosMasRecientesPrimero() {
    PlanBookmark newer = snapshot(publicPlan(PLAN_ID, "Nuevo"), user(USER_ID, EMAIL));
    PlanBookmark older = snapshot(publicPlan(OTHER_PLAN_ID, "Viejo"), user(USER_ID, EMAIL));
    when(this.userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.of(user(USER_ID, EMAIL)));
    when(this.planBookmarkRepositoryMock.findByUserId(USER_ID)).thenReturn(List.of(newer, older));
    when(this.planRepositoryMock.findById(PLAN_ID))
      .thenReturn(Optional.of(publicPlan(PLAN_ID, "Nuevo")));
    when(this.planRepositoryMock.findById(OTHER_PLAN_ID))
      .thenReturn(Optional.of(publicPlan(OTHER_PLAN_ID, "Viejo")));

    List<BookmarkEntry> entries = this.bookmarkService.getBookmarks(EMAIL);

    assertThat(entries.stream().map(BookmarkEntry::name).toList(), contains("Nuevo", "Viejo"));
  }

  @Test
  public void T_MARK_010_getBookmarks_soloLeeLasFilasDelUsuario() {
    when(this.userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.of(user(USER_ID, EMAIL)));
    when(this.planBookmarkRepositoryMock.findByUserId(USER_ID)).thenReturn(List.of());

    assertThat(this.bookmarkService.getBookmarks(EMAIL).isEmpty(), is(true));
    verify(this.planBookmarkRepositoryMock).findByUserId(USER_ID);
  }

  @Test
  public void T_MARK_011_getBookmarks_marcaAvailableCuandoElPlanSiguePublico() {
    PlanBookmark bookmark = snapshot(publicPlan(PLAN_ID), user(USER_ID, EMAIL));
    givenBookmark(bookmark, Optional.of(publicPlan(PLAN_ID)));

    BookmarkEntry entry = this.bookmarkService.getBookmarks(EMAIL).get(0);

    assertThat(entry.status(), is(BookmarkStatus.AVAILABLE));
    assertThat(entry.openable(), is(true));
    assertThat(entry.publicPlan(), is(true));
  }

  @Test
  public void T_MARK_012_getBookmarks_marcaDeletedCuandoElPlanYaNoExiste() {
    Plan plan = publicPlan(PLAN_ID, "Plan borrado");
    PlanBookmark bookmark = snapshot(plan, user(USER_ID, EMAIL));
    givenBookmark(bookmark, Optional.empty());

    BookmarkEntry entry = this.bookmarkService.getBookmarks(EMAIL).get(0);

    assertThat(entry.status(), is(BookmarkStatus.DELETED));
    assertThat(entry.openable(), is(false));
    assertThat(entry.name(), is("Plan borrado"));
    assertThat(entry.planId(), is(PLAN_ID));
  }

  @Test
  public void T_MARK_013_getBookmarks_marcaPrivateCuandoElPlanDejoDeSerAccesible() {
    Plan privatePlan = publicPlan(PLAN_ID);
    privatePlan.setIsPublic(false);
    PlanBookmark bookmark = snapshot(publicPlan(PLAN_ID), user(USER_ID, EMAIL));
    givenBookmark(bookmark, Optional.of(privatePlan));

    BookmarkEntry entry = this.bookmarkService.getBookmarks(EMAIL).get(0);

    assertThat(entry.status(), is(BookmarkStatus.PRIVATE));
    assertThat(entry.openable(), is(false));
  }

  @Test
  public void T_MARK_014_getBookmarks_dejaAvailableUnPlanPrivadoDelQueEsParticipante() {
    Plan privatePlan = publicPlan(PLAN_ID);
    privatePlan.setIsPublic(false);
    privatePlan.getParticipants().add(user(USER_ID, EMAIL));
    PlanBookmark bookmark = snapshot(publicPlan(PLAN_ID), user(USER_ID, EMAIL));
    givenBookmark(bookmark, Optional.of(privatePlan));

    BookmarkEntry entry = this.bookmarkService.getBookmarks(EMAIL).get(0);

    assertThat(entry.status(), is(BookmarkStatus.AVAILABLE));
    assertThat(entry.publicPlan(), is(false));
  }

  @Test
  public void T_MARK_014b_getBookmarks_deUnUsuarioInexistenteDevuelveListaVacia() {
    when(this.userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.empty());

    assertThat(this.bookmarkService.getBookmarks(EMAIL).isEmpty(), is(true));
  }

  // --- clone ---

  @Test
  public void T_MARK_015_cloneBookmark_conPlanVivoReusaElClon() {
    PlanBookmark bookmark = snapshot(publicPlan(PLAN_ID), user(USER_ID, EMAIL));
    when(this.userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.of(user(USER_ID, EMAIL)));
    when(this.planBookmarkRepositoryMock.findByUserIdAndPlanId(USER_ID, PLAN_ID))
      .thenReturn(Optional.of(bookmark));
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(publicPlan(PLAN_ID)));
    when(this.planServiceMock.clonePlan(PLAN_ID, EMAIL)).thenReturn(publicPlan(OTHER_PLAN_ID));

    this.bookmarkService.cloneBookmark(PLAN_ID, EMAIL);

    verify(this.planServiceMock).clonePlan(PLAN_ID, EMAIL);
    verify(this.planServiceMock, never()).createPlan(any(Plan.class), eq(EMAIL));
  }

  @Test
  public void T_MARK_016_cloneBookmark_deUnPlanBorradoReconstruyeElSnapshot() {
    Plan source = publicPlan(PLAN_ID, "Plan borrado");
    source.setDescription("Se conserva en la copia");
    source.setEventDate(LocalDate.of(2026, 11, 7));
    source.setEventTime(LocalTime.of(18, 30));
    PlanBookmark bookmark = snapshot(source, user(USER_ID, EMAIL));
    when(this.userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.of(user(USER_ID, EMAIL)));
    when(this.planBookmarkRepositoryMock.findByUserIdAndPlanId(USER_ID, PLAN_ID))
      .thenReturn(Optional.of(bookmark));
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.empty());
    when(this.placeRepositoryMock.findById(any())).thenReturn(Optional.of(place(PLACE_ID, "Cafe")));
    when(this.planServiceMock.createPlan(any(Plan.class), eq(EMAIL)))
      .thenAnswer(invocation -> invocation.getArgument(0));

    Plan copy = this.bookmarkService.cloneBookmark(PLAN_ID, EMAIL);

    assertThat(copy.getName(), is("Plan borrado"));
    assertThat(copy.getDescription(), is("Se conserva en la copia"));
    assertThat(copy.getEventDate(), is(LocalDate.of(2026, 11, 7)));
    assertThat(copy.getEventTime(), is(LocalTime.of(18, 30)));
    assertThat(copy.getIsPublic(), is(false));
    assertThat(copy.getPlanPlaces().size(), is(1));
    verify(this.planServiceMock, never()).clonePlan(any(), any());
  }

  @Test
  public void T_MARK_017_cloneBookmark_omiteLosLugaresQueYaNoExisten() {
    Plan source = publicPlan(PLAN_ID);
    source.getPlanPlaces().clear();
    source.addPlanPlace(entry(place(PLACE_ID, "Existe"), null, null, null, 1));
    source.addPlanPlace(entry(place(GONE_PLACE_ID, "No existe"), null, null, null, 2));
    PlanBookmark bookmark = snapshot(source, user(USER_ID, EMAIL));
    when(this.userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.of(user(USER_ID, EMAIL)));
    when(this.planBookmarkRepositoryMock.findByUserIdAndPlanId(USER_ID, PLAN_ID))
      .thenReturn(Optional.of(bookmark));
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.empty());
    when(this.placeRepositoryMock.findById(PLACE_ID))
      .thenReturn(Optional.of(place(PLACE_ID, "Existe")));
    when(this.placeRepositoryMock.findById(GONE_PLACE_ID)).thenReturn(Optional.empty());
    when(this.planServiceMock.createPlan(any(Plan.class), eq(EMAIL)))
      .thenAnswer(invocation -> invocation.getArgument(0));

    Plan copy = this.bookmarkService.cloneBookmark(PLAN_ID, EMAIL);

    assertThat(copy.getPlanPlaces().size(), is(1));
    assertThat(copy.getPlanPlaces().get(0).getPlace().getId(), is(PLACE_ID));
  }

  @Test
  public void T_MARK_017b_cloneBookmark_omiteLasParadasSinLugarAsociado() {
    PlanBookmark bookmark = snapshot(publicPlan(PLAN_ID), user(USER_ID, EMAIL));
    bookmark.getPlaces().get(0).setPlaceId(null);
    when(this.userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.of(user(USER_ID, EMAIL)));
    when(this.planBookmarkRepositoryMock.findByUserIdAndPlanId(USER_ID, PLAN_ID))
      .thenReturn(Optional.of(bookmark));
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.empty());
    when(this.planServiceMock.createPlan(any(Plan.class), eq(EMAIL)))
      .thenAnswer(invocation -> invocation.getArgument(0));

    Plan copy = this.bookmarkService.cloneBookmark(PLAN_ID, EMAIL);

    assertThat(copy.getPlanPlaces().isEmpty(), is(true));
    verify(this.placeRepositoryMock, never()).findById(any());
  }

  @Test
  public void T_MARK_018_cloneBookmark_deUnGuardadoAjenoRespondeComoInexistente() {
    when(this.userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.of(user(USER_ID, EMAIL)));
    when(this.planBookmarkRepositoryMock.findByUserIdAndPlanId(USER_ID, PLAN_ID))
      .thenReturn(Optional.empty());

    assertThrows(
      PlanNotFoundException.class,
      () -> this.bookmarkService.cloneBookmark(PLAN_ID, EMAIL)
    );
  }

  // --- flags de las cards ---

  @Test
  public void T_MARK_019_isBookmarked_y_getBookmarkedPlanIds_soloMiranLasFilasDelUsuario() {
    when(this.userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.of(user(USER_ID, EMAIL)));
    when(this.planBookmarkRepositoryMock.findByUserIdAndPlanId(USER_ID, PLAN_ID))
      .thenReturn(Optional.of(snapshot(publicPlan(PLAN_ID), user(USER_ID, EMAIL))));
    when(this.planBookmarkRepositoryMock.findPlanIdsByUserId(USER_ID)).thenReturn(Set.of(PLAN_ID));

    assertThat(this.bookmarkService.isBookmarked(PLAN_ID, EMAIL), is(true));
    assertThat(this.bookmarkService.getBookmarkedPlanIds(EMAIL), is(Set.of(PLAN_ID)));
    assertThat(this.bookmarkService.isBookmarked(PLAN_ID, null), is(false));
    assertThat(this.bookmarkService.isBookmarked(PLAN_ID, " "), is(false));
    assertThat(this.bookmarkService.getBookmarkedPlanIds(null), is(Set.of()));
    assertThat(this.bookmarkService.getBookmarkedPlanIds(" "), is(Set.of()));
  }

  @Test
  public void T_MARK_019b_isBookmarked_falseSiElUsuarioNoExisteOFaltaLaFila() {
    when(this.userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.empty());
    when(this.userRepositoryMock.findByEmail("otro@test.com"))
      .thenReturn(Optional.of(user(USER_ID, "otro@test.com")));
    when(this.planBookmarkRepositoryMock.findByUserIdAndPlanId(USER_ID, PLAN_ID))
      .thenReturn(Optional.empty());

    assertThat(this.bookmarkService.isBookmarked(PLAN_ID, EMAIL), is(false));
    assertThat(this.bookmarkService.isBookmarked(PLAN_ID, "otro@test.com"), is(false));
    assertThat(this.bookmarkService.getBookmarkedPlanIds(EMAIL), is(Set.of()));
    assertThat(this.bookmarkService.getBookmarkedPlanIds("otro@test.com"), is(Set.of()));
  }

  @Test
  public void T_MARK_019c_getBookmarks_sinAdministradorNoExponeAutor() {
    Plan plan = publicPlan(PLAN_ID);
    plan.setAdministrator(null);
    PlanBookmark bookmark = snapshot(plan, user(USER_ID, EMAIL));
    givenBookmark(bookmark, Optional.of(plan));

    BookmarkEntry entry = this.bookmarkService.getBookmarks(EMAIL).get(0);

    assertThat(entry.administratorId(), is(nullValue()));
    assertThat(entry.administratorName(), is(nullValue()));
  }

  private void givenBookmark(PlanBookmark bookmark, Optional<Plan> live) {
    when(this.userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.of(user(USER_ID, EMAIL)));
    when(this.planBookmarkRepositoryMock.findByUserId(USER_ID)).thenReturn(List.of(bookmark));
    when(this.planRepositoryMock.findById(bookmark.getPlanId())).thenReturn(live);
  }

  private PlanBookmark capturedBookmark() {
    ArgumentCaptor<PlanBookmark> captor = ArgumentCaptor.forClass(PlanBookmark.class);
    verify(this.planBookmarkRepositoryMock).save(captor.capture());
    return captor.getValue();
  }

  private static PlanBookmark snapshot(Plan plan, User user) {
    return PlanBookmark.snapshotOf(plan, user);
  }

  private static Plan publicPlan(UUID id) {
    return publicPlan(id, "Plan publico");
  }

  private static Plan publicPlan(UUID id, String name) {
    Plan plan = new Plan();
    plan.setId(id);
    plan.setName(name);
    plan.setIsPublic(true);
    plan.setAdministrator(user(AUTHOR_ID, "autor@test.com", "Ada", "Lovelace"));
    plan.addPlanPlace(entry(place(PLACE_ID, "Cafe"), "Primera parada", null, null, 1));
    return plan;
  }

  private static PlanPlace entry(
    Place place,
    String description,
    LocalDate visitDate,
    LocalTime visitTime,
    int sortOrder
  ) {
    PlanPlace entry = new PlanPlace();
    entry.setPlace(place);
    entry.setDescription(description);
    entry.setVisitDate(visitDate);
    entry.setVisitTime(visitTime);
    entry.setSortOrder(sortOrder);
    return entry;
  }

  private static Place place(UUID id, String name) {
    Place place = new Place();
    place.setId(id);
    place.setName(name);
    place.setCategory(PlaceCategory.CAFE);
    place.setAddress("Calle 1");
    return place;
  }

  private static User user(UUID id, String email) {
    return user(id, email, "Grace", "Hopper");
  }

  private static User user(UUID id, String email, String firstName, String lastName) {
    User user = new User();
    user.setId(id);
    user.setEmail(email);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    return user;
  }
}
