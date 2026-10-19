package com.valhalla.infrastructure;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
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

import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceRepository;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.rating.PlanRating;
import com.valhalla.domain.rating.PlanRatingRepository;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import com.valhalla.domain.user.UserService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

/**
 * Unit tests of {@link PublicPlanDataSeeder} against mocked ports.
 *
 * <p>El seeder esta apagado por defecto (las suites no lo quieren cerca), asi que la primera
 * pregunta es la llave: sin {@code -Dseed.demoPlans=true} no toca nada. Con la llave puesta,
 * cubre el caminito feliz — catalogo primero, fantasmas, planes publicos, calificaciones — y sus
 * guardas de idempotencia: sobre una base ya sembrada solo faltan las calificaciones nuevas.
 */
public class PublicPlanDataSeederTest {

  private static final String ENABLE_PROPERTY = "seed.demoPlans";
  private static final String FIRST_GHOST = "lucia.peralta@planit.demo";
  private static final List<String> EXPECTED_EMAILS = List.of(
    "lucia.peralta@planit.demo",
    "mateo.rios@planit.demo",
    "sofia.cabrera@planit.demo",
    "tomas.ledesma@planit.demo",
    "camila.ortega@planit.demo"
  );
  private static final List<String> ALL_PLACE_NAMES = List.of(
    "MALBA",
    "Planetario Galileo Galilei",
    "La Viruta",
    "Cafe Tortoni",
    "El Ateneo Grand Splendid",
    "Parque Tres de Febrero",
    "Cerveceria General San Martin",
    "Parrilla Don Julio",
    "Plaza Serrano"
  );
  /** Paradas por plan, en el orden de la lista PLANS del seeder. */
  private static final List<Integer> STOPS_PER_PLAN = List.of(2, 1, 2, 1, 1, 2);

  /**
   * Calificaciones fantasma por plan (16-CAL), en el orden de PLANS: {@code email:score}. La
   * distribucion del demo deja agregados variados (4.0, vacio x3, 3.7, 4.5) sin tocar planes con
   * calificaciones reales: son filas de exhibicion del feed.
   */
  private static final List<List<String>> EXPECTED_RATINGS = List.of(
    List.of(
      "mateo.rios@planit.demo:5",
      "sofia.cabrera@planit.demo:4",
      "tomas.ledesma@planit.demo:4",
      "camila.ortega@planit.demo:3"
    ),
    List.of(),
    List.of(),
    List.of(),
    List.of(
      "camila.ortega@planit.demo:5",
      "lucia.peralta@planit.demo:4",
      "sofia.cabrera@planit.demo:2"
    ),
    List.of("tomas.ledesma@planit.demo:4", "mateo.rios@planit.demo:5")
  );

  private UserService userServiceMock;
  private UserRepository userRepositoryMock;
  private PlanRepository planRepositoryMock;
  private PlaceRepository placeRepositoryMock;
  private PlaceDataSeeder placeDataSeederMock;
  private PlanRatingRepository planRatingRepositoryMock;
  private PublicPlanDataSeeder seeder;

  @BeforeEach
  public void init() {
    this.userServiceMock = mock(UserService.class);
    this.userRepositoryMock = mock(UserRepository.class);
    this.planRepositoryMock = mock(PlanRepository.class);
    this.placeRepositoryMock = mock(PlaceRepository.class);
    this.placeDataSeederMock = mock(PlaceDataSeeder.class);
    this.planRatingRepositoryMock = mock(PlanRatingRepository.class);
    this.seeder =
      new PublicPlanDataSeeder(
        this.userServiceMock,
        this.userRepositoryMock,
        this.planRepositoryMock,
        this.placeRepositoryMock,
        this.placeDataSeederMock,
        this.planRatingRepositoryMock
      );
    // El seeder usa el Plan devuelto por save (con el id asignado) para colgarle calificaciones:
    // el mock recicla el argumento, como hace Spring Data con una entidad nueva.
    when(this.planRepositoryMock.save(any(Plan.class)))
      .thenAnswer(invocation -> invocation.getArgument(0, Plan.class));
  }

  @AfterEach
  public void clearProperty() {
    System.clearProperty(ENABLE_PROPERTY);
  }

  @Test
  public void sinLaLlaveDeDemoNoTocaNada() {
    // given: sin -Dseed.demoPlans=true ni la variable de entorno (el default es false)

    // when
    this.seeder.onApplicationEvent(null);

    // then: ni el catalogo se pide, ni un solo fantasma
    verifyNoInteractions(
      this.userServiceMock,
      this.userRepositoryMock,
      this.planRepositoryMock,
      this.placeRepositoryMock,
      this.placeDataSeederMock,
      this.planRatingRepositoryMock
    );
  }

  @Test
  public void conLaLlavePuestaCargaCatalogoFantasmaYPlanesPublicos() {
    // given
    System.setProperty(ENABLE_PROPERTY, "true");
    givenFullCatalog();
    givenGhostsCanBeLookedUp(1);

    // when
    this.seeder.onApplicationEvent(null);

    // then: el catalogo se pide primero (el orden de los listeners no esta garantizado)
    InOrder order = inOrder(this.placeDataSeederMock, this.userServiceMock);
    order.verify(this.placeDataSeederMock).ensureSeeded();
    order
      .verify(this.userServiceMock, times(5))
      .create(anyString(), anyString(), anyString(), anyString(), anyString());

    // los cinco fantasmas existen, con rol USER
    for (String email : EXPECTED_EMAILS) {
      verify(this.userServiceMock)
        .create(eq(email), anyString(), eq("USER"), anyString(), anyString());
    }

    // seis planes publicos, con fecha futura, shortCode, dueño fantasma e itinerario
    ArgumentCaptor<Plan> captor = ArgumentCaptor.forClass(Plan.class);
    verify(this.planRepositoryMock, times(6)).save(captor.capture());
    List<Plan> plans = captor.getAllValues();
    for (int i = 0; i < plans.size(); i++) {
      Plan plan = plans.get(i);
      assertThat(plan.getIsPublic(), is(true));
      assertThat(plan.getShortCode(), is(notNullValue()));
      assertThat(plan.getAdministrator(), is(notNullValue()));
      assertThat(plan.getEventDate(), is(notNullValue()));
      assertThat(plan.getEventDate().isAfter(LocalDate.now()), is(true));
      assertThat(plan.getPlanPlaces().size(), is(equalTo(STOPS_PER_PLAN.get(i))));
      assertThat(plan.getPlanPlaces().get(0).getSortOrder(), is(equalTo(1)));
    }

    // [CAL] las calificaciones fantasma acompanan a sus planes, en el orden de PLANS
    ArgumentCaptor<PlanRating> ratingCaptor = ArgumentCaptor.forClass(PlanRating.class);
    verify(this.planRatingRepositoryMock, times(9)).save(ratingCaptor.capture());
    List<PlanRating> ratings = ratingCaptor.getAllValues();
    int i = 0;
    for (int planIndex = 0; planIndex < EXPECTED_RATINGS.size(); planIndex++) {
      for (String expected : EXPECTED_RATINGS.get(planIndex)) {
        int split = expected.indexOf(':');
        PlanRating rating = ratings.get(i++);
        assertThat(rating.getPlan(), is(equalTo(plans.get(planIndex))));
        assertThat(rating.getUser().getEmail(), is(equalTo(expected.substring(0, split))));
        assertThat(rating.getScore(), is(equalTo(Integer.parseInt(expected.substring(split + 1)))));
      }
    }
  }

  @Test
  public void conLosFantasmasYaCreadosSoloSeCompletanLasCalificaciones() {
    // given: la primera corrida ya dejo Lucia (y al resto) en la base, con sus planes
    System.setProperty(ENABLE_PROPERTY, "true");
    givenGhostsCanBeLookedUp(0);
    givenAllGhostPlansExist();

    // when
    this.seeder.onApplicationEvent(null);

    // then: nada se vuelve a crear, aca solo faltan las 9 calificaciones del demo
    verify(this.userServiceMock, never())
      .create(anyString(), anyString(), anyString(), anyString(), anyString());
    verify(this.placeDataSeederMock, never()).ensureSeeded();
    verify(this.planRepositoryMock, never()).save(any(Plan.class));
    ArgumentCaptor<PlanRating> ratingCaptor = ArgumentCaptor.forClass(PlanRating.class);
    verify(this.planRatingRepositoryMock, times(9)).save(ratingCaptor.capture());
    for (PlanRating rating : ratingCaptor.getAllValues()) {
      assertThat(rating.getPlan().getId(), is(notNullValue()));
      assertThat(rating.getUser(), is(notNullValue()));
    }
  }

  @Test
  public void lasCalificacionesYaSembradasNoSeDuplicanNiSePisan() {
    // given: base de una corrida anterior que ya tiene las calificaciones de esta version
    System.setProperty(ENABLE_PROPERTY, "true");
    givenCompletelySeededDatabase();

    // when
    this.seeder.onApplicationEvent(null);

    // then: la guarda por par (plan, rater) manda: ni una sola fila nueva ni updates
    verify(this.planRatingRepositoryMock, never()).save(any(PlanRating.class));
  }

  @Test
  public void siElCatalogoQuedaVacioNoSeGuardaNadaYSeReintentaEnElProximoEvento() {
    // given: la llave puesta pero el catalogo vacio aun tras pedirlo (ensureSeeded no pudo)
    System.setProperty(ENABLE_PROPERTY, "true");
    when(this.placeRepositoryMock.count()).thenReturn(0L);
    givenGhostsCanBeLookedUp(2);

    // when: el primer evento no siembra
    this.seeder.onApplicationEvent(null);

    // then
    verify(this.userServiceMock, never())
      .create(anyString(), anyString(), anyString(), anyString(), anyString());
    verify(this.planRepositoryMock, never()).save(any(Plan.class));
    verify(this.planRatingRepositoryMock, never()).save(any(PlanRating.class));

    // when: en el segundo el catalogo aparece, con un lugar desconocido para el plan que lo
    // tolera (la parada que ya no existe se salta, el resto del itinerario se arma igual)
    when(this.placeRepositoryMock.count()).thenReturn(10L);
    when(this.placeRepositoryMock.findAll()).thenReturn(placesNamed("MALBA"));
    this.seeder.onApplicationEvent(null);

    // then
    verify(this.planRepositoryMock, times(6)).save(any(Plan.class));
    verify(this.planRatingRepositoryMock, times(9)).save(any(PlanRating.class));
  }

  // --- fixtures ---

  private void givenFullCatalog() {
    when(this.placeRepositoryMock.count()).thenReturn(10L);
    when(this.placeRepositoryMock.findAll())
      .thenReturn(placesNamed(ALL_PLACE_NAMES.toArray(new String[0])));
  }

  /** Los 6 planes fantasma ya persistidos, con id y shortCode, como dejan las corridas previas. */
  private static final List<String> SHORT_CODES = List.of(
    "GHOST001",
    "GHOST002",
    "GHOST003",
    "GHOST004",
    "GHOST005",
    "GHOST006"
  );

  private void givenAllGhostPlansExist() {
    for (String shortCode : SHORT_CODES) {
      Plan plan = new Plan();
      plan.setId(UUID.randomUUID());
      plan.setShortCode(shortCode);
      when(this.planRepositoryMock.findByShortCode(shortCode)).thenReturn(Optional.of(plan));
    }
  }

  /** Base totalmente sembrada: fantasmas, planes y las 9 calificaciones de esta version. */
  private void givenCompletelySeededDatabase() {
    givenGhostsCanBeLookedUp(0);
    givenAllGhostPlansExist();
    when(this.planRatingRepositoryMock.findByPlanIdAndUserEmail(any(UUID.class), anyString()))
      .thenAnswer(invocation -> Optional.of(new PlanRating()));
  }

  private static List<Place> placesNamed(String... names) {
    List<Place> places = new ArrayList<>();
    long id = 1;
    for (String name : names) {
      Place place = new Place();
      place.setId(id++);
      place.setName(name);
      places.add(place);
    }
    return places;
  }

  /**
   * La guarda de idempotencia consulta a Lucia una vez por evento y despues de crearla cada
   * fantasma se relee para attachearlo a sus planes. Las consultas de la guarda (las primeras
   * {@code guardCalls}) responden vacias; la relectura, datos.
   */
  private void givenGhostsCanBeLookedUp(int guardCalls) {
    AtomicInteger lookups = new AtomicInteger();
    when(this.userRepositoryMock.findByEmail(FIRST_GHOST))
      .thenAnswer(invocation ->
        lookups.incrementAndGet() <= guardCalls
          ? Optional.empty()
          : Optional.of(userWith(FIRST_GHOST))
      );
    for (String email : EXPECTED_EMAILS.subList(1, EXPECTED_EMAILS.size())) {
      when(this.userRepositoryMock.findByEmail(email)).thenReturn(Optional.of(userWith(email)));
    }
  }

  private User userWith(String email) {
    User user = new User();
    user.setEmail(email);
    return user;
  }
}
