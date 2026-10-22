package com.valhalla.domain.bookmark;

/**
 * [MARK] Estado de un plan guardado, calculado por request contra el plan vivo. No se persiste: un
 * snapshot no puede saber si su plan sigue publico, se volvio privado o desaparecio.
 */
public enum BookmarkStatus {
  /** El plan existe y el usuario puede abrirlo (es publico, o es dueno/participante). */
  AVAILABLE,
  /** El plan existe pero dejo de ser accesible para este usuario. */
  PRIVATE,
  /** El plan ya no existe: el guardado queda solo con su snapshot. */
  DELETED,
}
