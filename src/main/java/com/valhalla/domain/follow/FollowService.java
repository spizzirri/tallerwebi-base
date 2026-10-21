package com.valhalla.domain.follow;

import com.valhalla.domain.user.User;
import java.util.List;

/**
 * [FOLLOW] Seguir y dejar de seguir usuarios, y las lecturas que alimentan el perfil y el feed.
 *
 * <p>Seguir es idempotente y no permite seguirse a uno mismo; dejar de seguir a quien no se sigue
 * es un no-op. Ninguna de las dos operaciones rompe la vista (AC-01..AC-03).
 */
public interface FollowService {
  void follow(Long followerId, Long followedId);

  void unfollow(Long followerId, Long followedId);

  boolean isFollowing(Long followerId, Long followedId);

  long countFollowers(Long userId);

  long countFollowing(Long userId);

  List<User> followers(Long userId);

  List<User> following(Long userId);

  /** [FOLLOW] Autores seguidos, para el scope Siguiendo del feed: una sola consulta (AC-15). */
  List<Long> followedIds(Long followerId);
}
