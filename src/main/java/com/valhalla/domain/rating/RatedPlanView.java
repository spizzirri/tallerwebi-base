package com.valhalla.domain.rating;

import java.util.UUID;

/**
 * [MYRAT] Una calificacion propia vista desde el perfil del usuario: el plan calificado, el puntaje
 * que el usuario le dio y si el plan sigue siendo accesible para el.
 *
 * <p>{@code accessible} reproduce la regla real de acceso del proyecto: el dueno y los participantes
 * entran siempre; un plan publico y activo lo lee cualquiera, salvo que el usuario este baneado. Un
 * plan borrado no llega hasta aca: [CAL] AC-12 borra sus calificaciones con el.
 *
 * @param planId plan calificado
 * @param planName nombre persistido del plan (el nombre que se muestra aun sin acceso)
 * @param score puntaje que el usuario le dio
 * @param accessible si el usuario puede abrir el plan hoy
 * @param member si el usuario es dueno o participante (define el link al detalle)
 */
public record RatedPlanView(
  UUID planId,
  String planName,
  int score,
  boolean accessible,
  boolean member
) {}
