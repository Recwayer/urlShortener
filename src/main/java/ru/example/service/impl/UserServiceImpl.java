package ru.example.service.impl;

import java.util.Optional;
import java.util.UUID;
import ru.example.domain.User;
import ru.example.repository.UserRepository;
import ru.example.service.UserService;

public class UserServiceImpl implements UserService {
  private final UserRepository repository;

  public UserServiceImpl(UserRepository repository) {
    this.repository = repository;
  }

  public User getOrCreateUser(UUID userId) {
    return repository.getOrCreate(userId);
  }

  public Optional<User> getUser(UUID userId) {
    return repository.findById(userId);
  }

  public User createNewUser() {
    User user = new User();
    repository.save(user);
    return user;
  }
}
