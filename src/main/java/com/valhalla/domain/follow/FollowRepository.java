package com.valhalla.domain.follow;

import com.valhalla.domain.user.User;
import java.util.List;
import java.util.UUID;

/**
 * [FOLLOW] Acceso a las relaciones de seguimiento entre usuarios.
 *
 * <p>No hay coleccion navegable en {@link User}: los conteos son agregados y las listas llegan
 * ordenadas por nombre desde la consulta, no por un {@code sort} en memoria.
 */
public interface FollowRepository {
  void save(Follow follow);

  boolean existsByFollowerIdAndFollowedId(UUID followerId, UUID followedId);

  void deleteByFollowerIdAndFollowedId(UUID followerId, UUID followedId);

  long countByFollowedId(UUID followedId);

  long countByFollowerId(UUID followerId);

  List<User> findFollowers(UUID followedId);

  List<User> findFollowing(UUID followerId);

  List<UUID> findFollowedIds(UUID followerId);
}
