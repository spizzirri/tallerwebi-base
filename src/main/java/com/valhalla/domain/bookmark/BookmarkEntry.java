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
 *
 * @param bookmarkId id del guardado
 * @param planId id del plan guardado
 * @param name nombre del plan
 * @param description descripcion del plan
 * @param eventDate fecha del evento
 * @param eventTime hora de inicio
 * @param placeCount cantidad de lugares del itinerario
 * @param administratorId id del autor del plan
 * @param administratorName nombre visible del autor
 * @param status estado del guardado
 * @param publicPlan si el plan es publico (card hacia la vista publica o la privada)
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
