package com.valhalla.presentation.user;

import com.valhalla.domain.user.User;
import java.util.Objects;
import java.util.UUID;

/**
 * [USEARCH] Fila del listado de personas: lo unico que la vista puede mostrar de otro usuario.
 *
 * <p>Ni email ni rol: nombre visible, avatar opcional, cantidad de planes publicos y el estado de
 * la relacion en el grafo. La identidad es el id, que solo se usa para linkear al perfil y para
 * armar el POST de seguir/dejar de seguir.
 *
 * @param id id del usuario
 * @param name nombre visible ya unido, sin email
 * @param avatarUrl avatar elegido, o {@code null} para caer a la inicial
 * @param publicPlanCount cantidad de planes publicos del usuario
 * @param following si el usuario de la sesion ya lo sigue
 */
public record UserSearchResult(
  UUID id,
  String name,
  String avatarUrl,
  long publicPlanCount,
  boolean following
) {
  /** Proyecta el usuario al unico payload publico que puede ver el buscador. */
  public static UserSearchResult of(User user, long publicPlanCount, boolean following) {
    String firstName = Objects.toString(user.getFirstName(), "");
    String lastName = Objects.toString(user.getLastName(), "");
    return new UserSearchResult(
      user.getId(),
      (firstName + " " + lastName).trim(),
      user.getAvatarUrl(),
      publicPlanCount,
      following
    );
  }
}
