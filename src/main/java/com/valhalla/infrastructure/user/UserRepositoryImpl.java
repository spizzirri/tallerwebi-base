package com.valhalla.infrastructure.user;

import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {

  private final JpaUserRepository jpaUserRepository;

  @Autowired
  public UserRepositoryImpl(JpaUserRepository jpaUserRepository) {
    this.jpaUserRepository = jpaUserRepository;
  }

  @Override
  public List<User> findAll() {
    return jpaUserRepository.findAll();
  }

  @Override
  public Optional<User> findById(UUID id) {
    return jpaUserRepository.findById(id);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return jpaUserRepository.findByEmail(email);
  }

  @Override
  public boolean existsByEmail(String email) {
    return jpaUserRepository.existsByEmail(email);
  }

  @Override
  public boolean existsById(UUID id) {
    return jpaUserRepository.existsById(id);
  }

  @Override
  public void save(User user) {
    jpaUserRepository.save(user);
  }

  @Override
  public void deleteById(UUID id) {
    jpaUserRepository.deleteById(id);
  }
}
