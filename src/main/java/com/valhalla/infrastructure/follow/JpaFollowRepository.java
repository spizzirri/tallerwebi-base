package com.valhalla.infrastructure.follow;

import com.valhalla.domain.follow.Follow;
import com.valhalla.domain.user.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * [FOLLOW] Consultas de la tabla {@code follows}. Los conteos son agregados de la base, no el
 * {@code size()} de una coleccion, y las listas ya salen ordenadas por nombre (AC-05, AC-15).
 */
public interface JpaFollowRepository extends JpaRepository<Follow, UUID> {
  boolean existsByFollowerIdAndFollowedId(UUID followerId, UUID followedId);

  long countByFollowedId(UUID followedId);

  long countByFollowerId(UUID followerId);

  void deleteByFollowerIdAndFollowedId(UUID followerId, UUID followedId);

  /** Seguidores de un usuario, ordenados por nombre y apellido sin distinguir mayusculas. */
  @Query(
    "select follow.follower from Follow follow where follow.followed.id = :userId " +
    "order by lower(follow.follower.firstName), lower(follow.follower.lastName), follow.follower.id"
  )
  List<User> findFollowers(@Param("userId") UUID userId);

  /** Usuarios seguidos por un usuario, con el mismo orden que la lista de seguidores. */
  @Query(
    "select follow.followed from Follow follow where follow.follower.id = :userId " +
    "order by lower(follow.followed.firstName), lower(follow.followed.lastName), follow.followed.id"
  )
  List<User> findFollowing(@Param("userId") UUID userId);

  /** [FOLLOW] Una sola consulta alimenta el scope Siguiendo: sin N+1 por plan (AC-15). */
  @Query("select follow.followed.id from Follow follow where follow.follower.id = :userId")
  List<UUID> findFollowedIds(@Param("userId") UUID userId);
}
