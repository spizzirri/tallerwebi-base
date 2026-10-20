package com.valhalla.domain.place;

import java.util.List;

/**
 * Puerto del importador hacia Overpass API. La implementacion con red vive en infrastructure, asi
 * los tests de integracion la sustituyen por un doble y ninguna suite depende de internet (AC-10);
 * cambiar el proveedor (por ejemplo a Photon) tampoco toca la vista.
 */
@FunctionalInterface
public interface OverpassClient {
  /**
   * POIs con nombre para un tag OSM dentro de una caja, por ejemplo
   * {@code findPois("amenity", "restaurant", BoundingBox.CABA)}.
   *
   * @param osmKey "amenity" o "shop"
   * @param osmValue el valor del tag, por ejemplo "restaurant" o "clothes"
   * @param box area visible del mapa, ya recortada a la ciudad
   * @return los POIs con nombre, sin elementos anonimos
   */
  List<OsmPoi> findPois(String osmKey, String osmValue, BoundingBox box);
}
