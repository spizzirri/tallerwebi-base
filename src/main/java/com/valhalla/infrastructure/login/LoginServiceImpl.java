package com.valhalla.infrastructure.login;

import com.valhalla.domain.exception.UserAlreadyExists;
import com.valhalla.domain.login.LoginService;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lives in infrastructure, not domain: the domain package must not depend on the framework. */
@Service
@Transactional
public class LoginServiceImpl implements LoginService {

  private static final String DEFAULT_ROLE = "USER";

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Autowired
  public LoginServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public User findUser(String email, String password) {
    User user = userRepository.findByEmail(email).orElse(null);
    if (
      user == null ||
      !Boolean.TRUE.equals(user.getActive()) ||
      !passwordEncoder.matches(password, user.getPassword())
    ) {
      return null;
    }
    return user;
  }

  @Override
  public void register(String email, String password) {
    register(email, password, null, null, null);
  }

  @Override
  public void register(String email, String password, String firstName, String lastName) {
    register(email, password, firstName, lastName, null);
  }

  @Override
  public void register(
    String email,
    String password,
    String firstName,
    String lastName,
    String avatarUrl
  ) {
    if (userRepository.findByEmail(email).isPresent()) {
      throw new UserAlreadyExists();
    }
    User user = new User();
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(password));
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setRole(DEFAULT_ROLE);
    user.setActive(true);
    user.setAvatarUrl(avatarUrl);
    userRepository.save(user);
  }
}
