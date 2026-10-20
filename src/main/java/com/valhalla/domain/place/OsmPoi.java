package com.valhalla.domain.place;

/**
 * Un POI de OpenStreetMap ya normalizado por el importador: solo los campos que el ABM necesita
 * para pre-llenar el form. {@code osmKey} es "amenity" o "shop" y {@code osmValue} el valor de ese
 * tag, que es lo que {@link PlaceCategory#fromOsm(String, String)} traduce a categoria.
 *
 * <p>Overpass no garantiza ninguno de los tags opcionales: telefono, web y horarios pueden quedar
 * en {@code null} y el form los muestra vacios.
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
  String osmValue
) {}
