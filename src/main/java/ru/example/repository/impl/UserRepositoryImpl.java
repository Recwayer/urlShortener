package ru.example.repository.impl;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import ru.example.domain.User;
import ru.example.repository.UserRepository;

public class UserRepositoryImpl implements UserRepository {
  private final Map<UUID, User> users = new ConcurrentHashMap<>();

  public User save(User user) {
    users.put(user.getId(), user);
    return user;
  }

  public Optional<User> findById(UUID id) {
    return Optional.ofNullable(users.get(id));
  }

  public User getOrCreate(UUID id) {
    return users.computeIfAbsent(id, k -> new User(id, java.time.LocalDateTime.now()));
  }
}
