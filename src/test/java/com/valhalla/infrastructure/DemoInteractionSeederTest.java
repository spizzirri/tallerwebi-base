package com.valhalla.infrastructure;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.valhalla.domain.bookmark.PlanBookmarkService;
import com.valhalla.domain.follow.Follow;
import com.valhalla.domain.follow.FollowRepository;
import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceRepository;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanPermission;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import com.valhalla.domain.user.UserService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

/**
 * Unit tests of {@link DemoInteractionSeeder} against mocked ports.
 *
 * <p>La primera pregunta es la llave: sin {@code seed.demoPlans} no toca nada, ni siquiera el
 * feed base. La segunda es el marcador {@code GHOST010}: con la llave puesta pero el marcador ya
 * sembrado, la corrida tambien se va. Con las dos guardas abiertas, el caminito feliz verifica los
 * cuatro grupos de filas y el orden que las reglas de los servicios exigen.
 */
public class DemoInteractionSeederTest {

  private static final String LUCIA_EMAIL = "lucia.peralta@planit.demo";
  private static final String MATEO_EMAIL = "mateo.rios@planit.demo";
  private static final String SOFIA_EMAIL = "sofia.cabrera@planit.demo";
  private static final String TOMAS_EMAIL = "tomas.ledesma@planit.demo";
  private static final String CAMILA_EMAIL = "camila.ortega@planit.demo";
  private static final String MARKER = "GHOST010";
  private static final String VANISHING = "GHOST011";
  private static final String EMPTY_LAYOUT = "GHOST012";
  private static final String PAST_LAYOUT = "GHOST013";
  private static final String BROKEN_IMAGE_URL = "https://example.invalid/foto.jpg";

  private PublicPlanDataSeeder publicPlanSeederMock;
  private UserService userServiceMock;
  private UserRepository userRepositoryMock;
  private PlanRepository planRepositoryMock;
  private PlaceRepository placeRepositoryMock;
  private PlanService planServiceMock;
  private PlanBookmarkService planBookmarkServiceMock;
  private FollowRepository followRepositoryMock;
  private DemoInteractionSeeder seeder;
  private List<Plan> savedPlans;

  @BeforeEach
  public void init() {
    this.publicPlanSeederMock = mock(PublicPlanDataSeeder.class);
    this.userServiceMock = mock(UserService.class);
    this.userRepositoryMock = mock(UserRepository.class);
    this.planRepositoryMock = mock(PlanRepository.class);
    this.placeRepositoryMock = mock(PlaceRepository.class);
    this.planServiceMock = mock(PlanService.class);
    this.planBookmarkServiceMock = mock(PlanBookmarkService.class);
    this.followRepositoryMock = mock(FollowRepository.class);
    this.savedPlans = new ArrayList<>();
    this.seeder =
      new DemoInteractionSeeder(
        this.publicPlanSeederMock,
        this.userServiceMock,
        this.userRepositoryMock,
        this.planRepositoryMock,
        this.placeRepositoryMock,
        this.planServiceMock,
        this.planBookmarkServiceMock,
        this.followRepositoryMock
      );
    // Spring Data devuelve la misma instancia con el id asignado: el mock le pone un id a la
    // entidad nueva y la recicla, como hace el repositorio real, y la deja para inspeccionarla.
    when(this.planRepositoryMock.save(any(Plan.class)))
      .thenAnswer(invocation -> {
        Plan plan = invocation.getArgument(0, Plan.class);
        if (plan.getId() == null) {
          plan.setId(UUID.randomUUID());
        }
        this.savedPlans.add(plan);
        return plan;
      });
  }

  @Test
  public void sinLaLlaveDeDemoNoTocaNada() {
    // given: el feed demo apagado (el default)

    // when
    this.seeder.onApplicationEvent(null);

    // then: ni el sembrado base se pide, ni un solo repositorio o servicio se toca
    verify(this.publicPlanSeederMock, never()).ensureSeeded();
    verifyNoInteractions(
      this.userServiceMock,
      this.userRepositoryMock,
      this.planRepositoryMock,
      this.placeRepositoryMock,
      this.planServiceMock,
      this.planBookmarkServiceMock,
      this.followRepositoryMock
    );
  }

  @Test
  public void conElMarcadorSembradoNoSiembraNada() {
    // given: el flag prendido y una base que ya paso por este seeder
    when(this.publicPlanSeederMock.isEnabled()).thenReturn(true);
    when(this.userRepositoryMock.findByEmail(LUCIA_EMAIL))
      .thenReturn(Optional.of(userWith(LUCIA_EMAIL)));
    when(this.planRepositoryMock.existsByShortCode(MARKER)).thenReturn(true);

    // when
    this.seeder.onApplicationEvent(null);

    // then: se pide el feed base y se lee el marcador, nada mas
    verify(this.publicPlanSeederMock).ensureSeeded();
    verify(this.userRepositoryMock).findByEmail(LUCIA_EMAIL);
    verify(this.planRepositoryMock).existsByShortCode(MARKER);
    verify(this.planRepositoryMock, never()).save(any(Plan.class));
    verifyNoInteractions(
      this.userServiceMock,
      this.placeRepositoryMock,
      this.planServiceMock,
      this.planBookmarkServiceMock,
      this.followRepositoryMock
    );
  }

  @Test
  public void sinFantasmasNoSiembraNada() {
    // given: el flag prendido pero el feed base no dejo fantasmas (catalogo vacio, por ejemplo)
    when(this.publicPlanSeederMock.isEnabled()).thenReturn(true);
    when(this.userRepositoryMock.findByEmail(LUCIA_EMAIL)).thenReturn(Optional.empty());

    // when
    this.seeder.onApplicationEvent(null);

    // then
    verifyNoInteractions(
      this.userServiceMock,
      this.placeRepositoryMock,
      this.planServiceMock,
      this.planBookmarkServiceMock,
      this.followRepositoryMock
    );
  }

  @Test
  public void siembraLosCuatroGruposDeInteracciones() {
    // given
    givenEnabledSeederWithGhosts();

    // when
    this.seeder.onApplicationEvent(null);

    // then: item 1 — tres guardados, uno por estado, y el plan borrado despues de guardarlo
    verify(this.planBookmarkServiceMock, times(3)).save(any(UUID.class), anyString());
    verify(this.planServiceMock, times(1))
      .deleteOwnedPlan(savedPlan(VANISHING).getId(), MATEO_EMAIL);

    // item 2 — la matriz de permisos y las filas de solicitud, rol e invitaciones
    verify(this.planServiceMock, times(1))
      .updateSettings(
        any(UUID.class),
        eq(PlanPermission.PARTICIPANTS),
        eq(PlanPermission.EDITORS),
        eq(PlanPermission.PARTICIPANTS),
        eq(PlanPermission.OWNER),
        eq(LUCIA_EMAIL)
      );
    verify(this.planServiceMock, times(1)).requestToJoin(any(UUID.class), eq(TOMAS_EMAIL));
    verify(this.planServiceMock, times(1))
      .changeParticipantRole(any(UUID.class), eq(MATEO_EMAIL), eq("editor"), eq(LUCIA_EMAIL));
    verify(this.planServiceMock, times(2))
      .inviteParticipant(any(UUID.class), anyString(), anyString(), anyString(), anyString());
    verify(this.planServiceMock, times(1))
      .inviteParticipant(
        any(UUID.class),
        eq(CAMILA_EMAIL),
        eq("editor"),
        eq("app"),
        eq(MATEO_EMAIL)
      );
    verify(this.planServiceMock, times(1))
      .inviteParticipant(
        any(UUID.class),
        eq(TOMAS_EMAIL),
        eq("guest"),
        eq("email"),
        eq(SOFIA_EMAIL)
      );

    // item 3 — los dos layouts raros
    Plan empty = savedPlan(EMPTY_LAYOUT);
    assertThat(empty.getPlanPlaces(), hasSize(0));
    assertThat(empty.getDescription().length(), greaterThan(1200));
    Plan past = savedPlan(PAST_LAYOUT);
    assertThat(past.getEventDate().isBefore(LocalDate.now()), is(true));
    assertThat(past.getPlanPlaces(), hasSize(2));
    assertThat(
      past.getPlanPlaces().get(0).getPlace().getImageUrl().startsWith("https://images."),
      is(true)
    );
    assertThat(past.getPlanPlaces().get(1).getPlace().getImageUrl(), is(equalTo(BROKEN_IMAGE_URL)));

    // item 4 — la cuenta desactivada y los tres seguidores de Camila
    verify(this.userServiceMock, times(1)).deactivate(any(UUID.class));
    ArgumentCaptor<Follow> followCaptor = ArgumentCaptor.forClass(Follow.class);
    verify(this.followRepositoryMock, times(3)).save(followCaptor.capture());
    for (Follow follow : followCaptor.getAllValues()) {
      assertThat(follow.getFollowed().getEmail(), is(equalTo(CAMILA_EMAIL)));
    }
    assertThat(
      followCaptor.getAllValues().stream().map(follow -> follow.getFollower().getEmail()).toList(),
      contains(LUCIA_EMAIL, MATEO_EMAIL, TOMAS_EMAIL)
    );

    // los cinco planes demo: GHOST010, GHOST011, el cierre de GHOST010 y los dos layouts
    assertThat(this.savedPlans, hasSize(5));
  }

  @Test
  public void guardaElPlanAntesDeCerrarloYHabilitaElSharingAntesDeLaSolicitud() {
    // given
    givenEnabledSeederWithGhosts();

    // when
    this.seeder.onApplicationEvent(null);

    // then: el guardado de Sofia y la solicitud de Tomas exigen estados opuestos del plan, asi
    // que el orden es el contrato: guardar publico, abrir sharing, cerrar, recien ahi pedir.
    Plan closing = savedPlan(MARKER);
    assertThat(closing.getIsPublic(), is(false));
    InOrder order = inOrder(this.planBookmarkServiceMock, this.planServiceMock);
    order.verify(this.planBookmarkServiceMock).save(closing.getId(), SOFIA_EMAIL);
    order
      .verify(this.planServiceMock)
      .updateSettings(
        eq(closing.getId()),
        eq(PlanPermission.PARTICIPANTS),
        eq(PlanPermission.EDITORS),
        eq(PlanPermission.PARTICIPANTS),
        eq(PlanPermission.OWNER),
        eq(LUCIA_EMAIL)
      );
    order.verify(this.planServiceMock).requestToJoin(closing.getId(), TOMAS_EMAIL);
  }

  @Test
  public void elSegundoEventoNoVuelveASembrar() {
    // given
    givenEnabledSeederWithGhosts();

    // when: devtools o un refresh del contexto disparan el evento dos veces
    this.seeder.onApplicationEvent(null);
    this.seeder.onApplicationEvent(null);

    // then: el flag en memoria corta la segunda corrida
    verify(this.publicPlanSeederMock, times(1)).ensureSeeded();
    assertThat(this.savedPlans, hasSize(5));
  }

  // --- fixtures ---

  private void givenEnabledSeederWithGhosts() {
    when(this.publicPlanSeederMock.isEnabled()).thenReturn(true);
    when(this.userRepositoryMock.findByEmail(anyString()))
      .thenAnswer(invocation -> Optional.of(userWith(invocation.getArgument(0, String.class))));
    when(this.planRepositoryMock.findByShortCode(anyString()))
      .thenAnswer(invocation -> {
        Plan plan = new Plan();
        plan.setId(UUID.randomUUID());
        plan.setShortCode(invocation.getArgument(0, String.class));
        plan.setIsPublic(true);
        return Optional.of(plan);
      });
    when(this.placeRepositoryMock.findAll())
      .thenReturn(placesNamed("MALBA", "Planetario Galileo Galilei", "Obelisco de Buenos Aires"));
  }

  /** El plan demo guardado con ese shortCode, con el id que le asigno el repositorio mockeado. */
  private Plan savedPlan(String shortCode) {
    return this.savedPlans.stream()
      .filter(plan -> shortCode.equals(plan.getShortCode()))
      .findFirst()
      .orElseThrow();
  }

  private static List<Place> placesNamed(String... names) {
    List<Place> places = new ArrayList<>();
    for (String name : names) {
      Place place = new Place();
      place.setId(UUID.randomUUID());
      place.setName(name);
      places.add(place);
    }
    return places;
  }

  private User userWith(String email) {
    User user = new User();
    user.setId(UUID.randomUUID());
    user.setEmail(email);
    return user;
  }
}
