package com.valhalla.domain.bookmark;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * [MARK] Modelo de lectura de un guardado para la seccion "Guardados" del perfil propio.
 *
 * <p>Mientras el plan siga accesible, los campos salen del <strong>plan vivo</strong> (reflejan las
 * ediciones del autor). Si no, salen del snapshot. {@code publicPlan} decide a que vista enlaza la
 * card: la publica o el detalle privado para dueno/participante.
 */
public record BookmarkEntry(
  UUID bookmarkId,
  UUID planId,
  String name,
  String description,
  LocalDate eventDate,
  LocalTime eventTime,
  int placeCount,
  UUID administratorId,
  String administratorName,
  BookmarkStatus status,
  boolean publicPlan
) {
  /** Solo un plan disponible se puede abrir; los otros dos estados se clonan o se quitan. */
  public boolean openable() {
    return status == BookmarkStatus.AVAILABLE;
  }
}
