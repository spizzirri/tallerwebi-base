package com.valhalla.domain.place;

import java.time.Instant;
import java.util.List;

/**
 * Lo que el importador le devuelve al navegador: las filas guardadas del area visible y cuando se
 * cargo por ultima vez la zona que las cubre. Son {@link MapPoi} y no {@link OsmPoi} porque el
 * sobre necesita la categoria y la identidad de la fila, que el fetch crudo no tiene.
 *
 * <p>{@code fetchedAt} en {@code null} no significa "no hay puntos": significa que esa zona nunca se
 * cargo, que es distinto de haberse cargado y no haber traido nada.
 *
 * @param pois filas guardadas del area visible
 * @param fetchedAt cuando se cargo la zona, o {@code null} si nunca se cargo
 */
public record ZonePois(List<MapPoi> pois, Instant fetchedAt) {}
