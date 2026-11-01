package com.valhalla.domain.place;

/**
 * Un POI de OpenStreetMap ya normalizado por el importador: solo los campos que el ABM necesita
 * para pre-llenar el form. {@code osmKey} es "amenity" o "shop" y {@code osmValue} el valor de ese
 * tag, que es lo que {@link PlaceCategory#fromOsm(String, String)} traduce a categoria.
 *
 * <p>Overpass no garantiza ninguno de los tags opcionales: telefono, web y horarios pueden quedar
 * en {@code null} y el form los muestra vacios.
 *
 * <p>{@code osmType} ({@code node}, {@code way}, {@code relation}) y {@code osmId} son la identidad
 * del elemento en OSM: lo unico que distingue dos POIs con el mismo nombre y permite actualizar una
 * fila en vez de duplicarla. La respuesta de {@code out center tags} siempre los trae, pero el
 * parser los tolera ausentes porque un fixture recortado no puede romper el importador.
 *
 * @param name nombre del POI
 * @param address direccion (puede ser {@code null})
 * @param latitude latitud del centro
 * @param longitude longitud del centro
 * @param openingHours horarios en formato OSM ({@code null} si no hay tag)
 * @param phone telefono ({@code null} si no hay tag)
 * @param website sitio web ({@code null} si no hay tag)
 * @param osmKey clave del tag ({@code amenity}, {@code shop}, ...)
 * @param osmValue valor del tag
 * @param osmType tipo de elemento OSM ({@code node}, {@code way}, {@code relation})
 * @param osmId id del elemento en OSM
 */
public record OsmPoi(
  String name,
  String address,
  Double latitude,
  Double longitude,
  String openingHours,
  String phone,
  String website,
  String osmKey,
  String osmValue,
  String osmType,
  Long osmId
) {}
