package com.valhalla.domain.follow;

import com.valhalla.domain.user.User;
import java.util.List;

/**
 * [FOLLOW] Acceso a las relaciones de seguimiento entre usuarios.
 *
 * <p>No hay coleccion navegable en {@link User}: los conteos son agregados y las listas llegan
 * ordenadas por nombre desde la consulta, no por un {@code sort} en memoria.
 */
public interface FollowRepository {
  void save(Follow follow);

  boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);

  void deleteByFollowerIdAndFollowedId(Long followerId, Long followedId);

  long countByFollowedId(Long followedId);

  long countByFollowerId(Long followerId);

  List<User> findFollowers(Long followedId);

  List<User> findFollowing(Long followerId);

  List<Long> findFollowedIds(Long followerId);
}
