package com.valhalla.domain.follow;

import com.valhalla.domain.user.User;
import java.util.List;
import java.util.UUID;

/**
 * [FOLLOW] Seguir y dejar de seguir usuarios, y las lecturas que alimentan el perfil y el feed.
 *
 * <p>Seguir es idempotente y no permite seguirse a uno mismo; dejar de seguir a quien no se sigue
 * es un no-op. Ninguna de las dos operaciones rompe la vista (AC-01..AC-03).
 */
public interface FollowService {
  void follow(UUID followerId, UUID followedId);

  void unfollow(UUID followerId, UUID followedId);

  boolean isFollowing(UUID followerId, UUID followedId);

  long countFollowers(UUID userId);

  long countFollowing(UUID userId);

  List<User> followers(UUID userId);

  List<User> following(UUID userId);

  /** [FOLLOW] Autores seguidos, para el scope Siguiendo del feed: una sola consulta (AC-15). */
  List<UUID> followedIds(UUID followerId);
}
