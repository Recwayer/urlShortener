package ru.example.repository;

import java.util.Optional;
import java.util.UUID;
import ru.example.domain.User;

public interface UserRepository {
  User save(User user);

  Optional<User> findById(UUID id);

  User getOrCreate(UUID id);
}
