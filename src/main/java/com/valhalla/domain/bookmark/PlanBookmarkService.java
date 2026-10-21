package com.valhalla.domain.bookmark;

import com.valhalla.domain.plan.Plan;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** [MARK] Planes publicos guardados por un usuario, para clonarlos cuando quiera. */
public interface PlanBookmarkService {
  /** Guarda un plan publico. Idempotente: si ya estaba guardado no escribe nada. */
  void save(UUID planId, String userEmail);

  /** Quita el guardado. Idempotente: si no estaba guardado no hace nada. */
  void remove(UUID planId, String userEmail);

  boolean isBookmarked(UUID planId, String userEmail);

  /** Ids guardados por el usuario, para pintar el icono de cada card en una sola pasada. */
  Set<UUID> getBookmarkedPlanIds(String userEmail);

  /** Guardados del usuario, mas recientes primero, con el estado calculado por request. */
  List<BookmarkEntry> getBookmarks(String userEmail);

  /**
   * Clona un plan guardado: con el plan vivo reusa [CLO]; si ya no esta disponible reconstruye un
   * plan privado propio desde el snapshot. El guardado tiene que ser del usuario.
   */
  Plan cloneBookmark(UUID planId, String userEmail);
}
