package ru.example.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {
  private final UUID id;
  private final LocalDateTime createdAt;

  public User() {
    this.id = UUID.randomUUID();
    this.createdAt = LocalDateTime.now();
  }

  public User(UUID id, LocalDateTime createdAt) {
    this.id = id;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
