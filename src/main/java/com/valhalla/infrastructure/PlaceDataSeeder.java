package com.valhalla.infrastructure;

import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceCategory;
import com.valhalla.domain.place.PlaceRepository;
import java.util.List;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class PlaceDataSeeder implements ApplicationListener<ContextRefreshedEvent> {

  private static final Logger LOGGER = Logger.getLogger(PlaceDataSeeder.class.getName());
  private final PlaceRepository placeRepository;
  private boolean seeded;

  private static final List<PlaceData> PLACES = List.of(
    new PlaceData(
      "El Sanjuanino",
      "Empanadas portenas clasicas y comida del norte",
      PlaceCategory.RESTAURANT,
      "Av. del Libertador 1234",
      -34.5895,
      -58.4095
    ),
    new PlaceData(
      "La Viruta",
      "Milonga de tango y escuela de baile en Palermo",
      PlaceCategory.NIGHTLIFE,
      "Armenia 1366",
      -34.6025,
      -58.4185
    ),
    new PlaceData(
      "MALBA",
      "Museo de Arte Latinoamericano de Buenos Aires",
      PlaceCategory.MUSEUM,
      "Av. Figueroa Alcorta 3415",
      -34.5833,
      -58.3928
    ),
    new PlaceData(
      "Plaza Serrano",
      "Plaza con feria de artesanos los domingos",
      PlaceCategory.SHOPPING,
      "Plaza Cortazar, Palermo",
      -34.5818,
      -58.4187
    ),
    new PlaceData(
      "Cafe Tortoni",
      "El cafe mas antiguo de Buenos Aires (1858)",
      PlaceCategory.CAFE,
      "Av. de Mayo 825",
      -34.6083,
      -58.3722
    ),
    new PlaceData(
      "Parque Tres de Febrero",
      "Gran parque con lagos y areas verdes",
      PlaceCategory.PARK,
      "Palermo",
      -34.5747,
      -58.4103
    ),
    new PlaceData(
      "El Ateneo Grand Splendid",
      "Libreria en un antiguo teatro",
      PlaceCategory.CULTURE,
      "Av. Santa Fe 1860",
      -34.5967,
      -58.3817
    ),
    new PlaceData(
      "Cerveceria General San Martin",
      "Cerveceria artesanal con shows en vivo",
      PlaceCategory.BAR,
      "Av. Corrientes 1475",
      -34.6033,
      -58.3817
    ),
    new PlaceData(
      "Planetario Galileo Galilei",
      "Planetario ubicado en el Parque Tres de Febrero",
      PlaceCategory.CULTURE,
      "Av. Sarmiento s/n",
      -34.5725,
      -58.4172
    ),
    new PlaceData(
      "Parrilla Don Julio",
      "Parrilla galardonada a nivel internacional",
      PlaceCategory.RESTAURANT,
      "Guatemala 4699",
      -34.5892,
      -58.4262
    )
  );

  @Autowired
  public PlaceDataSeeder(PlaceRepository placeRepository) {
    this.placeRepository = placeRepository;
  }

  @Override
  public synchronized void onApplicationEvent(ContextRefreshedEvent event) {
    ensureSeeded();
  }

  /**
   * Idempotente: carga el catalogo solo si esta vacio. Es publico porque el seeder del feed
   * [PPV] puede llegar a correr antes que este (el orden de los listeners no esta garantizado)
   * y necesita los lugares ya cargados para armar los itinerarios.
   */
  public synchronized void ensureSeeded() {
    if (seeded || placeRepository.count() > 0) {
      return;
    }
    seeded = true;
    PLACES.forEach(this::savePlace);
    LOGGER.info("Place seed data loaded");
  }

  private void savePlace(PlaceData data) {
    Place place = new Place();
    place.setName(data.name());
    place.setDescription(data.description());
    place.setCategory(data.category());
    place.setAddress(data.address());
    place.setLatitude(data.latitude());
    place.setLongitude(data.longitude());
    placeRepository.save(place);
  }

  private record PlaceData(
    String name,
    String description,
    PlaceCategory category,
    String address,
    double latitude,
    double longitude
  ) {}
}
