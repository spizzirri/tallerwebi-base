package com.valhalla.infrastructure;

import com.valhalla.domain.bookmark.PlanBookmarkService;
import com.valhalla.domain.follow.Follow;
import com.valhalla.domain.follow.FollowRepository;
import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceCategory;
import com.valhalla.domain.place.PlaceRepository;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanPermission;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import com.valhalla.domain.user.UserService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * [DEMO] Siembra los estados de interaccion que una base recien reseteada no alcanza clickeando:
 * los tres estados de un guardado (disponible, privado, borrado), la matriz de permisos de un
 * plan, los layouts que solo se rompen con datos raros y las cuentas fuera de lo normal.
 *
 * <p>Cuelga del mismo flag que el feed demo ({@code -Dseed.demoPlans=true}) y corre despues de
 * {@link PublicPlanDataSeeder}, al que le pide el sembrado base a mano porque el orden de los
 * listeners no esta garantizado. La guarda de idempotencia es una sola: el shortCode
 * {@code GHOST010}. Si ya esta, esta corrida no toca nada; un seed a medias se repara con
 * {@code reset-db}, no con guardas por fila.
 */
@Component
public class DemoInteractionSeeder implements ApplicationListener<ContextRefreshedEvent> {

  private static final Logger LOGGER = Logger.getLogger(DemoInteractionSeeder.class.getName());

  private static final String MARKER_SHORT_CODE = "GHOST010";
  private static final String VANISHING_SHORT_CODE = "GHOST011";
  private static final String EMPTY_SHORT_CODE = "GHOST012";
  private static final String PAST_SHORT_CODE = "GHOST013";

  private static final String LUCIA = "lucia.peralta@planit.demo";
  private static final String MATEO = "mateo.rios@planit.demo";
  private static final String SOFIA = "sofia.cabrera@planit.demo";
  private static final String TOMAS = "tomas.ledesma@planit.demo";
  private static final String CAMILA = "camila.ortega@planit.demo";
  private static final List<String> GHOST_EMAILS = List.of(LUCIA, MATEO, SOFIA, TOMAS, CAMILA);

  private static final String INACTIVE_EMAIL = "inactivo@planit.demo";
  private static final String INACTIVE_PASSWORD = "password";
  private static final String INACTIVE_FIRST_NAME = "Inactivo";
  private static final String INACTIVE_LAST_NAME = "Demo";

  private static final String PARTICIPANT_ROLE = "guest";
  private static final String EDITOR_ROLE = "editor";
  private static final String APP_CHANNEL = "app";
  private static final String EMAIL_CHANNEL = "email";
  private static final String USER_ROLE = "USER";

  /** Foto real: la card la pinta. */
  private static final String GOOD_IMAGE_URL =
    "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800";
  /** Foto rota: el navegador la pide, falla, y la vista tiene que caer al placeholder. */
  private static final String BROKEN_IMAGE_URL = "https://example.invalid/foto.jpg";

  /** Frase base de la descripcion larga (GHOST012): el detalle tiene que cortar sin romper. */
  private static final String LONG_SENTENCE =
    "Nos juntamos a charlar de todo un poco, sin apuro y sin itinerario fijo. ";
  private static final String LONG_DESCRIPTION = LONG_SENTENCE.repeat(20);

  /** Plan demo: nace publico porque el shortCode es el marcador y el guardado exige publico. */
  private record DemoPlan(
    String shortCode,
    String name,
    String description,
    int daysFromNow,
    String hour,
    String ownerEmail,
    List<String> participantEmails,
    List<String> placeNames
  ) {}

  private static final DemoPlan CLOSING_PLAN = new DemoPlan(
    MARKER_SHORT_CODE,
    "Cierre en la terraza",
    "Asado, música y brindis con vista a la ciudad. Traigan algo para la parrilla.",
    9,
    "20:00",
    LUCIA,
    List.of(CAMILA),
    List.of("MALBA", "Planetario Galileo Galilei")
  );

  private static final DemoPlan VANISHING_PLAN = new DemoPlan(
    VANISHING_SHORT_CODE,
    "Escapada a la costa",
    "Dos días de playa y noches de fogón. Salimos el viernes a la tardecita.",
    16,
    "18:00",
    MATEO,
    List.of(SOFIA),
    List.of("Obelisco de Buenos Aires")
  );

  private static final DemoPlan EMPTY_LAYOUT = new DemoPlan(
    EMPTY_SHORT_CODE,
    "Charla de sobremesa",
    LONG_DESCRIPTION,
    25,
    "19:00",
    CAMILA,
    List.of(),
    List.of()
  );

  private static final DemoPlan PAST_LAYOUT = new DemoPlan(
    PAST_SHORT_CODE,
    "Vuelta por los bares de siempre",
    "Recorrido corto para ver como se ve un plan que ya pasó.",
    -21,
    "11:00",
    TOMAS,
    List.of(),
    List.of()
  );

  private final PublicPlanDataSeeder publicPlanSeeder;
  private final UserService userService;
  private final UserRepository userRepository;
  private final PlanRepository planRepository;
  private final PlaceRepository placeRepository;
  private final PlanService planService;
  private final PlanBookmarkService planBookmarkService;
  private final FollowRepository followRepository;

  private boolean seeded;

  public DemoInteractionSeeder(
    PublicPlanDataSeeder publicPlanSeeder,
    UserService userService,
    UserRepository userRepository,
    PlanRepository planRepository,
    PlaceRepository placeRepository,
    PlanService planService,
    PlanBookmarkService planBookmarkService,
    FollowRepository followRepository
  ) {
    this.publicPlanSeeder = publicPlanSeeder;
    this.userService = userService;
    this.userRepository = userRepository;
    this.planRepository = planRepository;
    this.placeRepository = placeRepository;
    this.planService = planService;
    this.planBookmarkService = planBookmarkService;
    this.followRepository = followRepository;
  }

  @Override
  public synchronized void onApplicationEvent(ContextRefreshedEvent event) {
    if (seeded || !publicPlanSeeder.isEnabled()) {
      return;
    }
    seeded = true;
    // El orden de los listeners no esta garantizado: el feed base y su catalogo se piden a mano,
    // igual que hace PublicPlanDataSeeder, y los dos son idempotentes.
    publicPlanSeeder.ensureSeeded();
    if (!ghostsPresent() || planRepository.existsByShortCode(MARKER_SHORT_CODE)) {
      return;
    }
    seed();
    if (LOGGER.isLoggable(Level.INFO)) {
      LOGGER.info(
        "Demo interaction seed loaded: 3 bookmark states, plan permissions, 2 layouts, " +
        "1 inactive account, 3 follows"
      );
    }
  }

  private void seed() {
    Map<String, User> ghosts = ghostsByEmail();
    Map<String, Plan> plans = createDemoPlans(ghosts);
    seedBookmarks(plans);
    seedPermissions(plans);
    seedLayouts(ghosts);
    seedAccounts(ghosts);
  }

  private boolean ghostsPresent() {
    return userRepository.findByEmail(LUCIA).isPresent();
  }

  private Map<String, User> ghostsByEmail() {
    Map<String, User> ghosts = new LinkedHashMap<>();
    for (String email : GHOST_EMAILS) {
      ghosts.put(email, userRepository.findByEmail(email).orElseThrow());
    }
    return ghosts;
  }

  private Map<String, Plan> createDemoPlans(Map<String, User> ghosts) {
    Map<String, Place> catalogue = placesByName();
    Map<String, Plan> plans = new LinkedHashMap<>();
    plans.put(MARKER_SHORT_CODE, savePlan(CLOSING_PLAN, ghosts, catalogue));
    plans.put(VANISHING_SHORT_CODE, savePlan(VANISHING_PLAN, ghosts, catalogue));
    return plans;
  }

  /**
   * Los planes demo se arman con {@code new Plan()} + save, nunca con
   * {@code planService.createPlan}: ese servicio siempre regenera el shortCode y los GHOSTxxx
   * son el marcador de idempotencia.
   */
  private Plan savePlan(DemoPlan data, Map<String, User> ghosts, Map<String, Place> catalogue) {
    Plan plan = newPlan(data, ghosts.get(data.ownerEmail()));
    for (String email : data.participantEmails()) {
      plan.getParticipants().add(ghosts.get(email));
    }
    int order = 1;
    for (String placeName : data.placeNames()) {
      Place place = catalogue.get(placeName);
      if (place == null) {
        continue; // El catalogo cambio: el plan se arma con las paradas que sigan existiendo.
      }
      addStop(plan, place, order);
      order++;
    }
    return planRepository.save(plan);
  }

  private Plan newPlan(DemoPlan data, User owner) {
    Plan plan = new Plan();
    plan.setName(data.name());
    plan.setDescription(data.description());
    plan.setEventDate(LocalDate.now().plusDays(data.daysFromNow()));
    plan.setEventTime(LocalTime.parse(data.hour()));
    plan.setIsPublic(true);
    plan.setShortCode(data.shortCode());
    plan.setAdministrator(owner);
    return plan;
  }

  private Map<String, Place> placesByName() {
    return placeRepository
      .findAll()
      .stream()
      .collect(
        Collectors.toMap(Place::getName, Function.identity(), (a, b) -> a, LinkedHashMap::new)
      );
  }

  private Plan findPlan(String shortCode) {
    return planRepository.findByShortCode(shortCode).orElseThrow();
  }

  /** Item 1: los tres estados de un guardado, en el orden que las reglas del servicio exigen. */
  private void seedBookmarks(Map<String, Plan> plans) {
    // AVAILABLE: el plan sigue publico, la card se arma con el plan vivo.
    this.planBookmarkService.save(findPlan("GHOST002").getId(), SOFIA);
    // PRIVATE: se guarda publico — guardar rechaza un plan privado — y seedPermissions lo cierra.
    this.planBookmarkService.save(plans.get(MARKER_SHORT_CODE).getId(), SOFIA);
    // DELETED: el snapshot sobrevive al plan; el dueño lo borra al cerrar el item.
    Plan vanishing = plans.get(VANISHING_SHORT_CODE);
    this.planBookmarkService.save(vanishing.getId(), TOMAS);
    this.planService.deleteOwnedPlan(vanishing.getId(), MATEO);
  }

  /**
   * Item 2: la matriz de permisos. El orden de GHOST010 es obligatorio: primero el cierre, despues
   * el sharing, y recien entonces la solicitud de ingreso. El cierre va antes de {@code
   * updateSettings} a proposito: guardar esta entidad despues reescribiria los permisos recien
   * seteados con los que trae en memoria, y el plan seguiria sin sharing.
   */
  private void seedPermissions(Map<String, Plan> plans) {
    Plan closing = plans.get(MARKER_SHORT_CODE);
    closing.setIsPublic(false);
    this.planRepository.save(closing);
    this.planService.updateSettings(
        closing.getId(),
        PlanPermission.PARTICIPANTS,
        PlanPermission.EDITORS,
        PlanPermission.PARTICIPANTS,
        PlanPermission.OWNER,
        LUCIA
      );
    this.planService.requestToJoin(closing.getId(), TOMAS);

    // GHOST001: Mateo deja de ser un participante mas y pasa a editor.
    this.planService.changeParticipantRole(findPlan("GHOST001").getId(), MATEO, EDITOR_ROLE, LUCIA);
    // GHOST002: invitacion interna, canal app.
    this.planService.inviteParticipant(
        findPlan("GHOST002").getId(),
        CAMILA,
        EDITOR_ROLE,
        APP_CHANNEL,
        MATEO
      );
    // GHOST003: invitacion por email (sin SMTP cae en el notifier que solo loguea) y settings
    // abiertos a participantes, para que el plan no sea solo del dueño.
    this.planService.inviteParticipant(
        findPlan("GHOST003").getId(),
        TOMAS,
        PARTICIPANT_ROLE,
        EMAIL_CHANNEL,
        SOFIA
      );
    this.planService.updateSettings(
        findPlan("GHOST003").getId(),
        PlanPermission.PARTICIPANTS,
        PlanPermission.EDITORS,
        PlanPermission.PARTICIPANTS,
        PlanPermission.PARTICIPANTS,
        SOFIA
      );
  }

  /** Item 3: los dos layouts raros. GHOST012 y GHOST013 nacen aca, no en el catalogo. */
  private void seedLayouts(Map<String, User> ghosts) {
    this.planRepository.save(newPlan(EMPTY_LAYOUT, ghosts.get(CAMILA)));

    Plan past = newPlan(PAST_LAYOUT, ghosts.get(TOMAS));
    addStop(past, savePlace("Bar Ideal", GOOD_IMAGE_URL), 1);
    addStop(past, savePlace("Confiteria del Molino", BROKEN_IMAGE_URL), 2);
    this.planRepository.save(past);
  }

  /** Item 4: una cuenta desactivada y Camila con tres seguidores y cero seguidos. */
  private void seedAccounts(Map<String, User> ghosts) {
    this.userService.create(
        INACTIVE_EMAIL,
        INACTIVE_PASSWORD,
        USER_ROLE,
        INACTIVE_FIRST_NAME,
        INACTIVE_LAST_NAME
      );
    User inactive = this.userRepository.findByEmail(INACTIVE_EMAIL).orElseThrow();
    this.userService.deactivate(inactive.getId());

    User camila = ghosts.get(CAMILA);
    for (String email : List.of(LUCIA, MATEO, TOMAS)) {
      this.followRepository.save(new Follow(ghosts.get(email), camila));
    }
  }

  private void addStop(Plan plan, Place place, int order) {
    PlanPlace stop = new PlanPlace();
    stop.setPlace(place);
    stop.setSortOrder(order);
    plan.addPlanPlace(stop);
  }

  private Place savePlace(String name, String imageUrl) {
    Place place = new Place();
    place.setName(name);
    place.setDescription("Parada del recorrido demo, con foto cargada.");
    place.setCategory(PlaceCategory.BAR);
    place.setAddress("Buenos Aires");
    place.setLatitude(-34.6037);
    place.setLongitude(-58.3816);
    place.setImageUrl(imageUrl);
    this.placeRepository.save(place);
    return place;
  }
}
