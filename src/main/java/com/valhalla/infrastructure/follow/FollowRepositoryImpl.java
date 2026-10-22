package com.valhalla.infrastructure.follow;

import com.valhalla.domain.follow.Follow;
import com.valhalla.domain.follow.FollowRepository;
import com.valhalla.domain.user.User;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/** [FOLLOW] Adaptador del puerto de dominio sobre Spring Data. */
@Repository
public class FollowRepositoryImpl implements FollowRepository {

  private final JpaFollowRepository jpaFollowRepository;

  @Autowired
  public FollowRepositoryImpl(JpaFollowRepository jpaFollowRepository) {
    this.jpaFollowRepository = jpaFollowRepository;
  }

  @Override
  public void save(Follow follow) {
    jpaFollowRepository.save(follow);
  }

  @Override
  public boolean existsByFollowerIdAndFollowedId(UUID followerId, UUID followedId) {
    return jpaFollowRepository.existsByFollowerIdAndFollowedId(followerId, followedId);
  }

  @Override
  public void deleteByFollowerIdAndFollowedId(UUID followerId, UUID followedId) {
    jpaFollowRepository.deleteByFollowerIdAndFollowedId(followerId, followedId);
  }

  @Override
  public long countByFollowedId(UUID followedId) {
    return jpaFollowRepository.countByFollowedId(followedId);
  }

  @Override
  public long countByFollowerId(UUID followerId) {
    return jpaFollowRepository.countByFollowerId(followerId);
  }

  @Override
  public List<User> findFollowers(UUID followedId) {
    return jpaFollowRepository.findFollowers(followedId);
  }

  @Override
  public List<User> findFollowing(UUID followerId) {
    return jpaFollowRepository.findFollowing(followerId);
  }

  @Override
  public List<UUID> findFollowedIds(UUID followerId) {
    return jpaFollowRepository.findFollowedIds(followerId);
  }
}
