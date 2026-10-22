package com.valhalla.infrastructure.follow;

import com.valhalla.domain.follow.Follow;
import com.valhalla.domain.follow.FollowRepository;
import com.valhalla.domain.follow.FollowService;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserService;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [FOLLOW] Reglas de seguir/dejar de seguir. El unique de la tabla es la ultima defensa; este
 * servicio es la primera: seguir dos veces es un no-op y dejar de seguir a quien no se sigue no
 * escribe nada (AC-02).
 *
 * <p>Los dos usuarios se resuelven con {@link UserService#findById(Long)} para que un id
 * inexistente salga como {@code UserNotFoundException} y el perfil responda 404 (AC-01).
 */
@Service
public class FollowServiceImpl implements FollowService {

  private final FollowRepository followRepository;
  private final UserService userService;

  @Autowired
  public FollowServiceImpl(FollowRepository followRepository, UserService userService) {
    this.followRepository = followRepository;
    this.userService = userService;
  }

  @Override
  @Transactional
  public void follow(Long followerId, Long followedId) {
    if (Objects.equals(followerId, followedId)) {
      throw new IllegalArgumentException("No podes seguirte a vos mismo");
    }
    User follower = userService.findById(followerId);
    User followed = userService.findById(followedId);
    if (followRepository.existsByFollowerIdAndFollowedId(followerId, followedId)) {
      return;
    }
    followRepository.save(new Follow(follower, followed));
  }

  @Override
  @Transactional
  public void unfollow(Long followerId, Long followedId) {
    if (!followRepository.existsByFollowerIdAndFollowedId(followerId, followedId)) {
      return;
    }
    followRepository.deleteByFollowerIdAndFollowedId(followerId, followedId);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isFollowing(Long followerId, Long followedId) {
    return followRepository.existsByFollowerIdAndFollowedId(followerId, followedId);
  }

  @Override
  @Transactional(readOnly = true)
  public long countFollowers(Long userId) {
    return followRepository.countByFollowedId(userId);
  }

  @Override
  @Transactional(readOnly = true)
  public long countFollowing(Long userId) {
    return followRepository.countByFollowerId(userId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<User> followers(Long userId) {
    return followRepository.findFollowers(userId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<User> following(Long userId) {
    return followRepository.findFollowing(userId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Long> followedIds(Long followerId) {
    return followRepository.findFollowedIds(followerId);
  }
}
