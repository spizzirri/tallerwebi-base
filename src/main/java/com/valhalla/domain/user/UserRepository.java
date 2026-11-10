package com.valhalla.domain.user;

import com.valhalla.domain.exception.UserNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
  List<User> findAll();
  /** [USEARCH] Query accent-insensitive del nombre/apellido, con el propio usuario excluido. */
  List<User> searchByName(String normalizedQuery, UUID excludeUserId);
  Optional<User> findById(UUID id);
  Optional<User> findByEmail(String email);
  boolean existsByEmail(String email);
  boolean existsById(UUID id);
  void save(User user);
  void deleteById(UUID id);

  default void update(User user) {
    if (user.getId() == null) {
      throw new UserNotFoundException();
    }
    if (!existsById(user.getId())) {
      throw new UserNotFoundException();
    }
    save(user);
  }
}
