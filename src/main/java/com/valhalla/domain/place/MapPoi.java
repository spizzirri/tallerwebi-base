package com.valhalla.domain.place;

import com.valhalla.domain.plan.UuidVersion7Generator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

/**
 * Un POI de Overpass que ya se trajo y quedo guardado: es lo que pinta el importador sin volver a
 * llamar al servicio publico. [POI]
 *
 * <p>La identidad es {@code sourceKey} ({@code osm:way/123}), no el par nombre+coordenadas: dos
 * esquinas pueden llamarse igual y un mismo punto puede moverse unos metros sin dejar de ser el
 * mismo. La unique es sobre esa columna, y sobrevive a que el elemento no traiga id (ahi la clave
 * cae a nombre+coordenadas, que es peor pero es lo unico honesto que queda).
 *
 * <p>No hay timestamp por fila: la frescura del conjunto la lleva {@link PoiZone}. Y no hay FK a
 * {@code places}: el catalogo curado sigue siendo otra cosa, que el admin acepta o no.
 *
 * <p>Los accessors son exactamente los que el codigo usa. Hibernate entra por campo (el {@code @Id}
 * esta en el field) y la cobertura de {@code domain} esta pineada al 100%: un getter de mas es una
 * linea muerta que rompe el build.
 */
@Entity
@Table(
  name = "poi",
  uniqueConstraints = @UniqueConstraint(columnNames = "source_key"),
  indexes = @Index(name = "idx_poi_position", columnList = "latitude, longitude")
)
public class MapPoi { // NOPMD — intentional: 15 fields vs maxfields=10; the extras are the OSM identity (sourceKey, osmType, osmId), the tag it was fetched with and four optional contact columns, plain columns with no behaviour.

  @Id
  @UuidGenerator(algorithm = UuidVersion7Generator.class)
  private UUID id;

  @Column(name = "source_key", nullable = false, length = 1024)
  private String sourceKey;

  @Column(name = "osm_type", length = 32)
  private String osmType;

  @Column(name = "osm_id")
  private Long osmId;

  @Column(name = "tag_key", nullable = false, length = 32)
  private String tagKey;

  @Column(name = "tag_value", nullable = false, length = 128)
  private String tagValue;

  @Column(nullable = false)
  private String name;

  private String address;

  private Double latitude;
  private Double longitude;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PlaceCategory category;

  @Column(name = "opening_hours")
  private String openingHours;

  private String phone;

  private String website;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private PoiStatus status;

  public MapPoi() {}

  /** Alta de un POI recien traido: {@code tagKey}/{@code tagValue} son los de la consulta. */
  public static MapPoi from(OsmPoi poi, PlaceCategory category, String tagKey, String tagValue) {
    MapPoi entity = new MapPoi();
    entity.sourceKey = sourceKeyOf(poi);
    entity.osmType = poi.osmType();
    entity.osmId = poi.osmId();
    entity.tagKey = tagKey;
    entity.tagValue = tagValue;
    entity.name = poi.name();
    entity.address = poi.address();
    entity.latitude = poi.latitude();
    entity.longitude = poi.longitude();
    entity.category = category;
    entity.openingHours = poi.openingHours();
    entity.phone = poi.phone();
    entity.website = poi.website();
    entity.status = PoiStatus.ACTIVE;
    return entity;
  }

  /**
   * Clave natural del elemento en OSM. Sin {@code id} se cae a nombre+coordenadas: no distingue dos
   * POIs homonimos en el mismo punto, pero evita que un elemento incompleto entre duplicado en cada
   * carga.
   */
  public static String sourceKeyOf(OsmPoi poi) {
    if (poi.osmType() != null && poi.osmId() != null) {
      return "osm:" + poi.osmType() + "/" + poi.osmId();
    }
    return "fallback:" + poi.name() + "@" + poi.latitude() + "," + poi.longitude();
  }

  /**
   * Copia los campos visibles del POI traido y avisa si algo cambio.
   *
   * <p>La comparacion es por {@link OsmPoi} entero en vez de campo por campo: si mañana el importador
   * gana un dato, entra solo y no queda un diff a medias. La identidad y el tag no se comparan
   * porque son la clave con la que se encontro esta fila.
   *
   * @return {@code true} solo si algun valor mostrado difiere y hubo que escribir
   */
  public boolean apply(OsmPoi poi, PlaceCategory newCategory) {
    OsmPoi incoming = new OsmPoi(
      poi.name(),
      poi.address(),
      poi.latitude(),
      poi.longitude(),
      poi.openingHours(),
      poi.phone(),
      poi.website(),
      tagKey,
      tagValue,
      osmType,
      osmId
    );
    if (incoming.equals(toOsmPoi()) && this.category == newCategory) {
      return false;
    }
    this.name = incoming.name();
    this.address = incoming.address();
    this.latitude = incoming.latitude();
    this.longitude = incoming.longitude();
    this.category = newCategory;
    this.openingHours = incoming.openingHours();
    this.phone = incoming.phone();
    this.website = incoming.website();
    return true;
  }

  /** Vista de lectura: el mismo record que el importador ya le devuelve al navegador. */
  public OsmPoi toOsmPoi() {
    return new OsmPoi(
      name,
      address,
      latitude,
      longitude,
      openingHours,
      phone,
      website,
      tagKey,
      tagValue,
      osmType,
      osmId
    );
  }

  public UUID getId() {
    return id;
  }

  public String getSourceKey() {
    return sourceKey;
  }

  public void setSourceKey(String sourceKey) {
    this.sourceKey = sourceKey;
  }

  public Long getOsmId() {
    return osmId;
  }

  public String getName() {
    return name;
  }

  public String getAddress() {
    return address;
  }

  public Double getLatitude() {
    return latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public PlaceCategory getCategory() {
    return category;
  }

  public String getOpeningHours() {
    return openingHours;
  }

  public String getPhone() {
    return phone;
  }

  public String getWebsite() {
    return website;
  }

  public PoiStatus getStatus() {
    return status;
  }

  public void setStatus(PoiStatus status) {
    this.status = status;
  }
}
