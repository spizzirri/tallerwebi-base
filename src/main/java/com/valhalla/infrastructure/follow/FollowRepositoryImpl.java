package com.valhalla.infrastructure.follow;

import com.valhalla.domain.follow.Follow;
import com.valhalla.domain.follow.FollowRepository;
import com.valhalla.domain.user.User;
import java.util.List;
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
  public boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId) {
    return jpaFollowRepository.existsByFollowerIdAndFollowedId(followerId, followedId);
  }

  @Override
  public void deleteByFollowerIdAndFollowedId(Long followerId, Long followedId) {
    jpaFollowRepository.deleteByFollowerIdAndFollowedId(followerId, followedId);
  }

  @Override
  public long countByFollowedId(Long followedId) {
    return jpaFollowRepository.countByFollowedId(followedId);
  }

  @Override
  public long countByFollowerId(Long followerId) {
    return jpaFollowRepository.countByFollowerId(followerId);
  }

  @Override
  public List<User> findFollowers(Long followedId) {
    return jpaFollowRepository.findFollowers(followedId);
  }

  @Override
  public List<User> findFollowing(Long followerId) {
    return jpaFollowRepository.findFollowing(followerId);
  }

  @Override
  public List<Long> findFollowedIds(Long followerId) {
    return jpaFollowRepository.findFollowedIds(followerId);
  }
}
