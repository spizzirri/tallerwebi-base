package com.valhalla.infrastructure;

import com.valhalla.domain.follow.Follow;
import com.valhalla.domain.follow.FollowRepository;
import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceRepository;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.plan.PlanScheduleCalculator;
import com.valhalla.domain.plan.TransportMode;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.rating.PlanRating;
import com.valhalla.domain.rating.PlanRatingRepository;
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
 * [PPV] Usuarios y planes fantasma para que el feed nunca arranque vacio: como el muro de
 * Instagram, el valor de la vista es ver que otros publicaron y decidir si lo usas, lo editas o
 * armas el tuyo.
 *
 * <p>Apagado por defecto — las suites de test (HSQLDB) y el arranque normal no lo quieren cerca.
 * Se enciende con {@code -Dseed.demoPlans=true}, que es lo que hace {@code docker-dev.sh} en el
 * perfil dev. Es idempotente: si los fantasmas ya existen, no vuelve a crear ni usuarios ni
 * planes; las calificaciones fantasma ([CAL]) se completan aparte, por si la base viene de una
 * corrida anterior a que el seeder las sembrara.
 *
 * <p>[SCH] Los itinerarios demo salen con horario derivado: cada parada trae su duracion de la
 * categoria y las horas las calcula {@link PlanScheduleCalculator} con la ventana y el modo del
 * plan, en vez de venir escritas a mano aca.
 */
@Component
public class PublicPlanDataSeeder implements ApplicationListener<ContextRefreshedEvent> {

  private static final Logger LOGGER = Logger.getLogger(PublicPlanDataSeeder.class.getName());

  private static final String ENABLE_PROPERTY = "seed.demoPlans";
  private static final String ENABLE_ENV = "SEED_DEMO_PLANS";
  // Password de mentira: los fantasmas son personajes del feed, nadie inicia sesion como ellos.
  private static final String GHOST_PASSWORD = "fantasma";

  private static final List<String> GHOST_EMAILS = List.of(
    "lucia.peralta@planit.demo",
    "mateo.rios@planit.demo",
    "sofia.cabrera@planit.demo",
    "tomas.ledesma@planit.demo",
    "camila.ortega@planit.demo"
  );

  private static final List<String> GHOST_FIRST_NAMES = List.of(
    "Lucía",
    "Mateo",
    "Sofía",
    "Tomás",
    "Camila"
  );

  private static final List<String> GHOST_LAST_NAMES = List.of(
    "Peralta",
    "Ríos",
    "Cabrera",
    "Ledesma",
    "Ortega"
  );

  private record GhostPlan(
    String shortCode,
    String name,
    String description,
    int daysFromNow,
    String hour,
    String dayEnd,
    TransportMode transportMode,
    int ownerIndex,
    List<Integer> participantIndexes,
    List<String> placeNames,
    List<RatingSeed> ratings
  ) {}

  /** [CAL] Calificacion fantasma: el fantasma en el indice {@code raterIndex} da score estrellas. */
  private record RatingSeed(int raterIndex, int score) {}

  /** [FOLLOW] Arista demo: el fantasma {@code followerIndex} sigue al {@code followedIndex}. */
  private record FollowSeed(int followerIndex, int followedIndex) {}

  /**
   * [FOLLOW] Relaciones demo entre fantasmas: el scope Siguiendo arranca con al menos dos planes
   * publicos (lucia sigue a mateo y a sofia, duenos de GHOST002 y GHOST003).
   */
  private static final List<FollowSeed> FOLLOWS = List.of(
    new FollowSeed(0, 1),
    new FollowSeed(0, 2),
    new FollowSeed(1, 0),
    new FollowSeed(2, 3)
  );

  /** [SCH] Los seis planes demo comparten la misma ventana: el dia cierra a las 23:00. */
  private static final String DAY_END = "23:00";

  private static final List<GhostPlan> PLANS = List.of(
    new GhostPlan(
      "GHOST001",
      "Sábado de museos",
      "MALBA a la mañana y el Planetario cuando cae la tarde. Traigan plata para la cafetería.",
      4,
      "14:00",
      DAY_END,
      TransportMode.BUS,
      0,
      List.of(1, 2),
      List.of("MALBA", "Planetario Galileo Galilei"),
      // [CAL] promedio 4.0 con 4 votos, ninguno del owner: el feed luce con agregados variados
      List.of(
        new RatingSeed(1, 5),
        new RatingSeed(2, 4),
        new RatingSeed(3, 4),
        new RatingSeed(4, 3)
      )
    ),
    new GhostPlan(
      "GHOST002",
      "Milonga para principiantes",
      "Nadie sabe bailar, igual vamos. Clase a las 21:00 y después tira libre.",
      7,
      "21:00",
      DAY_END,
      TransportMode.BUS,
      1,
      List.of(3),
      List.of("La Viruta"),
      List.of() // sin calificaciones: cubre el estado vacio de la fila (AC-07) en el demo
    ),
    new GhostPlan(
      "GHOST003",
      "Café, libros y conversación",
      "Empezamos en el Tortoni charlando y terminamos mirando estanterías en el Ateneo.",
      10,
      "11:00",
      DAY_END,
      TransportMode.WALK,
      2,
      List.of(4),
      List.of("Cafe Tortoni", "El Ateneo Grand Splendid"),
      List.of()
    ),
    new GhostPlan(
      "GHOST004",
      "Asado en el parque",
      "Todos llevan algo: carne, ensalada, hielo o el postre. El que no lleva nada lava los platos.",
      13,
      "13:00",
      DAY_END,
      TransportMode.BIKE,
      3,
      List.of(0, 1, 2, 4),
      List.of("Parque Tres de Febrero"),
      List.of()
    ),
    new GhostPlan(
      "GHOST005",
      "Cerveza artesanal y show en vivo",
      "La cervecería de Corrientes tiene banda los sábados. Reservamos mesa atrás.",
      17,
      "20:00",
      DAY_END,
      TransportMode.BUS,
      4,
      List.of(0),
      List.of("Cerveceria General San Martin"),
      // [CAL] promedio 3.7 con 3 votos: el owner califica su propio plan (AC-06),
      // un participante le pega 4 y un tercero ajeno al plan le pega 2
      List.of(new RatingSeed(4, 5), new RatingSeed(0, 4), new RatingSeed(2, 2))
    ),
    new GhostPlan(
      "GHOST006",
      "Parrillada de fin de mes",
      "El Don Julio no acepta reserva así que vamos temprano y paseamos por la feria de Serrano.",
      21,
      "20:00",
      DAY_END,
      TransportMode.CAR,
      0,
      List.of(1),
      List.of("Parrilla Don Julio", "Plaza Serrano"),
      // [CAL] promedio 4.5 con 2 votos, con media estrella en el relleno (4.5 -> 90%)
      List.of(new RatingSeed(3, 4), new RatingSeed(1, 5))
    )
  );

  private final UserService userService;
  private final UserRepository userRepository;
  private final PlanRepository planRepository;
  private final PlaceRepository placeRepository;
  private final PlaceDataSeeder placeDataSeeder;
  private final PlanRatingRepository planRatingRepository;
  private final FollowRepository followRepository;

  private boolean seeded;

  public PublicPlanDataSeeder(
    UserService userService,
    UserRepository userRepository,
    PlanRepository planRepository,
    PlaceRepository placeRepository,
    PlaceDataSeeder placeDataSeeder,
    PlanRatingRepository planRatingRepository,
    FollowRepository followRepository
  ) {
    this.userService = userService;
    this.userRepository = userRepository;
    this.planRepository = planRepository;
    this.placeRepository = placeRepository;
    this.placeDataSeeder = placeDataSeeder;
    this.planRatingRepository = planRatingRepository;
    this.followRepository = followRepository;
  }

  @Override
  public synchronized void onApplicationEvent(ContextRefreshedEvent event) {
    ensureSeeded();
  }

  /**
   * Idempotente: siembra fantasmas, planes publicos, calificaciones y follows. Es publico porque
   * {@link DemoInteractionSeeder} cuelga de este feed (le pone guardados, permisos e invitaciones
   * encima) y el orden de los listeners no esta garantizado.
   */
  public synchronized void ensureSeeded() {
    if (seeded || !enabled()) {
      return;
    }
    if (!ghostsAlreadyExist()) {
      // El orden de los listeners no esta garantizado: el catalogo de lugares se pide a mano,
      // idempotente, para que los itinerarios fantasmas no dependan de quien corra primero.
      placeDataSeeder.ensureSeeded();
      if (placeRepository.count() == 0) {
        return;
      }
      seeded = true;

      Map<String, User> ghosts = createGhosts();
      Map<String, Place> placesByName = placesByName();
      for (GhostPlan data : PLANS) {
        seedRatings(savePlan(data, ghosts, placesByName), data, ghosts);
      }
      // [FOLLOW] Relaciones demo: el scope Siguiendo del feed arranca con contenido real.
      seedFollows(ghosts);
      if (LOGGER.isLoggable(Level.INFO)) {
        LOGGER.info(
          "Public demo feed seeded: " + GHOST_EMAILS.size() + " ghosts, " + PLANS.size() + " plans"
        );
      }
      return;
    }
    // Base de una corrida anterior: ni fantasmas ni planes se recrean, aca solo faltan las
    // calificaciones de esta version del seeder. Idempotente por par (plan shortCode, rater):
    // lo ya calificado no se toca.
    seeded = true;
    Map<String, User> ghosts = existingGhosts();
    int added = completeRatings(ghosts);
    // [FOLLOW] Misma logica para las relaciones: solo se crean las que faltan (AC-14).
    seedFollows(ghosts);
    if (added > 0 && LOGGER.isLoggable(Level.INFO)) {
      LOGGER.info("Public demo ratings topped up: " + added);
    }
  }

  /** Encendido solo donde se pide: {@code -Dseed.demoPlans=true} o la variable de entorno. */
  boolean isEnabled() {
    return enabled();
  }

  /** Encendido solo donde se pide: {@code -Dseed.demoPlans=true} o la variable de entorno. */
  private boolean enabled() {
    String fallback = System.getenv().getOrDefault(ENABLE_ENV, "false");
    return Boolean.parseBoolean(System.getProperty(ENABLE_PROPERTY, fallback));
  }

  private boolean ghostsAlreadyExist() {
    return userRepository.findByEmail(GHOST_EMAILS.get(0)).isPresent();
  }

  private Map<String, User> createGhosts() {
    Map<String, User> ghosts = new LinkedHashMap<>();
    for (int i = 0; i < GHOST_EMAILS.size(); i++) {
      String email = GHOST_EMAILS.get(i);
      userService.create(
        email,
        GHOST_PASSWORD,
        "USER",
        GHOST_FIRST_NAMES.get(i),
        GHOST_LAST_NAMES.get(i)
      );
      ghosts.put(email, userRepository.findByEmail(email).orElseThrow());
    }
    return ghosts;
  }

  private Map<String, Place> placesByName() {
    return placeRepository
      .findAll()
      .stream()
      .collect(
        Collectors.toMap(Place::getName, Function.identity(), (a, b) -> a, LinkedHashMap::new)
      );
  }

  private Plan savePlan(GhostPlan data, Map<String, User> ghosts, Map<String, Place> placesByName) {
    Plan plan = new Plan();
    plan.setName(data.name());
    plan.setDescription(data.description());
    plan.setEventDate(LocalDate.now().plusDays(data.daysFromNow()));
    plan.setEventTime(LocalTime.parse(data.hour()));
    plan.setDayEndTime(LocalTime.parse(data.dayEnd()));
    plan.setTransportMode(data.transportMode());
    plan.setIsPublic(true);
    plan.setShortCode(data.shortCode());
    plan.setAdministrator(ghosts.get(GHOST_EMAILS.get(data.ownerIndex())));
    for (int participantIndex : data.participantIndexes()) {
      plan.getParticipants().add(ghosts.get(GHOST_EMAILS.get(participantIndex)));
    }

    int order = 1;
    for (String placeName : data.placeNames()) {
      Place place = placesByName.get(placeName);
      if (place == null) {
        continue; // El catalogo cambio: el plan se arma con las paradas que sigan existiendo.
      }
      PlanPlace entry = new PlanPlace();
      entry.setPlace(place);
      entry.setSortOrder(order);
      // [SCH] La duracion del demo es la de su categoria, explicita para que el horario quede
      // estable aunque cambien los defaults del catalogo.
      entry.setDurationMinutes(PlanScheduleCalculator.defaultDurationMinutes(place.getCategory()));
      order++;
      plan.addPlanPlace(entry);
    }
    applyDerivedSchedule(plan);
    // Spring Data devuelve la misma instancia con el id asignado, que es el que usan las
    // calificaciones del plan recien creado.
    return planRepository.save(plan);
  }

  /**
   * [SCH] El seeder no inventa horas a mano: las deriva con la misma calculadora que usa el resto
   * del sistema, con la ventana y el modo de traslado del plan. Asi el itinerario demo se ve igual
   * que uno armado desde el form, y cambiar una duracion del catalogo mueve las horas solas.
   */
  private void applyDerivedSchedule(Plan plan) {
    List<PlanScheduleCalculator.Stop> inputs = plan
      .getPlanPlaces()
      .stream()
      .map(stop ->
        new PlanScheduleCalculator.Stop(
          null,
          null,
          stop.getDurationMinutes(),
          stop.getPlace().getDefaultDurationMinutes(),
          stop.getPlace().getCategory(),
          stop.getPlace().getLatitude(),
          stop.getPlace().getLongitude(),
          stop.getTransportModeToNext(),
          stop.getTravelMinutesToNext()
        )
      )
      .toList();
    List<PlanScheduleCalculator.Slot> slots = PlanScheduleCalculator.schedule(
      plan.getEventDate(),
      plan.getEventTime(),
      plan.getDayEndTime(),
      plan.getTransportMode(),
      inputs
    );
    for (int i = 0; i < slots.size(); i++) {
      plan.getPlanPlaces().get(i).setVisitDate(slots.get(i).date());
      plan.getPlanPlaces().get(i).setVisitTime(slots.get(i).start());
    }
  }

  /**
   * [CAL] Da de alta las calificaciones fantasma del plan que falten: idempotente por par
   * (plan, rater) con la misma unique constraint que la tabla, asi una base ya sembrada no
   * duplica ni pisa nada.
   *
   * @return la cantidad de calificaciones nuevas
   */
  private int seedRatings(Plan plan, GhostPlan data, Map<String, User> ghosts) {
    int added = 0;
    for (RatingSeed seed : data.ratings()) {
      String raterEmail = GHOST_EMAILS.get(seed.raterIndex());
      if (planRatingRepository.findByPlanIdAndUserEmail(plan.getId(), raterEmail).isPresent()) {
        continue;
      }
      planRatingRepository.save(new PlanRating(plan, ghosts.get(raterEmail), seed.score()));
      added++;
    }
    return added;
  }

  /**
   * [FOLLOW] Siembra las relaciones demo que falten. Idempotente por par (follower, followed):
   * el unique de la tabla es la ultima defensa, esta consulta es la primera (AC-14).
   *
   * @return la cantidad de relaciones nuevas
   */
  private int seedFollows(Map<String, User> ghosts) {
    int added = 0;
    for (FollowSeed seed : FOLLOWS) {
      User follower = ghosts.get(GHOST_EMAILS.get(seed.followerIndex()));
      User followed = ghosts.get(GHOST_EMAILS.get(seed.followedIndex()));
      if (follower == null || followed == null) {
        continue; // Base a medio sembrar: se completa en la proxima corrida.
      }
      if (followRepository.existsByFollowerIdAndFollowedId(follower.getId(), followed.getId())) {
        continue;
      }
      followRepository.save(new Follow(follower, followed));
      added++;
    }
    return added;
  }

  /** Fantasmas ya persistidos, indexados por email como los devuelve {@code createGhosts}. */
  private Map<String, User> existingGhosts() {
    Map<String, User> ghosts = new LinkedHashMap<>();
    for (String email : GHOST_EMAILS) {
      userRepository.findByEmail(email).ifPresent(user -> ghosts.put(email, user));
    }
    return ghosts;
  }

  /** Completar calificaciones sobre planes y fantasmas ya existentes (corridas previas). */
  private int completeRatings(Map<String, User> ghosts) {
    int added = 0;
    for (GhostPlan data : PLANS) {
      Plan plan = planRepository.findByShortCode(data.shortCode()).orElse(null);
      if (plan == null) {
        continue;
      }
      added += seedRatings(plan, data, ghosts);
    }
    return added;
  }
}
