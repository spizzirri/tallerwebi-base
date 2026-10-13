package com.valhalla.infrastructure;

import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceRepository;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
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
 * [PPV] Usuarios y planes fantasma para que el feed nunca arranque vacio: como el muro de
 * Instagram, el valor de la vista es ver que otros publicaron y decidir si lo usas, lo editas o
 * armas el tuyo.
 *
 * <p>Apagado por defecto — las suites de test (HSQLDB) y el arranque normal no lo quieren cerca.
 * Se enciende con {@code -Dseed.demoPlans=true}, que es lo que hace {@code docker-dev.sh} en el
 * perfil dev. Es idempotente: si los fantasmas ya existen, no vuelve a tocar nada.
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
    int ownerIndex,
    List<Integer> participantIndexes,
    List<String> placeNames
  ) {}

  private static final List<GhostPlan> PLANS = List.of(
    new GhostPlan(
      "GHOST001",
      "Sábado de museos",
      "MALBA a la mañana y el Planetario cuando cae la tarde. Traigan plata para la cafetería.",
      4,
      "14:00",
      0,
      List.of(1, 2),
      List.of("MALBA", "Planetario Galileo Galilei")
    ),
    new GhostPlan(
      "GHOST002",
      "Milonga para principiantes",
      "Nadie sabe bailar, igual vamos. Clase a las 21:30 y después tira libre.",
      7,
      "21:30",
      1,
      List.of(3),
      List.of("La Viruta")
    ),
    new GhostPlan(
      "GHOST003",
      "Café, libros y conversación",
      "Empezamos en el Tortoni charlando y terminamos mirando estanterías en el Ateneo.",
      10,
      "11:00",
      2,
      List.of(4),
      List.of("Cafe Tortoni", "El Ateneo Grand Splendid")
    ),
    new GhostPlan(
      "GHOST004",
      "Asado en el parque",
      "Todos llevan algo: carne, ensalada, hielo o el postre. El que no lleva nada lava los platos.",
      13,
      "13:00",
      3,
      List.of(0, 1, 2, 4),
      List.of("Parque Tres de Febrero")
    ),
    new GhostPlan(
      "GHOST005",
      "Cerveza artesanal y show en vivo",
      "La cervecería de Corrientes tiene banda los sábados. Reservamos mesa atrás.",
      17,
      "20:00",
      4,
      List.of(0),
      List.of("Cerveceria General San Martin")
    ),
    new GhostPlan(
      "GHOST006",
      "Parrillada de fin de mes",
      "El Don Julio no acepta reserva así que vamos temprano y paseamos por la feria de Serrano.",
      21,
      "20:30",
      0,
      List.of(1),
      List.of("Parrilla Don Julio", "Plaza Serrano")
    )
  );

  private final UserService userService;
  private final UserRepository userRepository;
  private final PlanRepository planRepository;
  private final PlaceRepository placeRepository;
  private final PlaceDataSeeder placeDataSeeder;

  private boolean seeded;

  public PublicPlanDataSeeder(
    UserService userService,
    UserRepository userRepository,
    PlanRepository planRepository,
    PlaceRepository placeRepository,
    PlaceDataSeeder placeDataSeeder
  ) {
    this.userService = userService;
    this.userRepository = userRepository;
    this.planRepository = planRepository;
    this.placeRepository = placeRepository;
    this.placeDataSeeder = placeDataSeeder;
  }

  @Override
  public synchronized void onApplicationEvent(ContextRefreshedEvent event) {
    if (seeded || !enabled() || ghostsAlreadyExist()) {
      return;
    }
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
      savePlan(data, ghosts, placesByName);
    }
    if (LOGGER.isLoggable(Level.INFO)) {
      LOGGER.info(
        "Public demo feed seeded: " + GHOST_EMAILS.size() + " ghosts, " + PLANS.size() + " plans"
      );
    }
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

  private void savePlan(GhostPlan data, Map<String, User> ghosts, Map<String, Place> placesByName) {
    Plan plan = new Plan();
    plan.setName(data.name());
    plan.setDescription(data.description());
    plan.setEventDate(LocalDate.now().plusDays(data.daysFromNow()));
    plan.setEventTime(LocalTime.parse(data.hour()));
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
      order++;
      plan.addPlanPlace(entry);
    }
    planRepository.save(plan);
  }
}
