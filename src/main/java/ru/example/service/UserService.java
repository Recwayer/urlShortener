package ru.example.service;

import java.util.Optional;
import java.util.UUID;
import ru.example.domain.User;

public interface UserService {
  User getOrCreateUser(UUID userId);

  Optional<User> getUser(UUID userId);

  User createNewUser();
}
