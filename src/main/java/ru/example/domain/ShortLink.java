package ru.example.domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public class ShortLink {
  private final UUID id;
  private final LocalDateTime createdAt;
  private final String originalUrl;
  private final UUID ownerId;
  private final String shortCode;
  private int clickCount;
  private final int maxClicks;
  private final Duration ttl;
  private boolean active;

  public ShortLink(
      String originalUrl, UUID ownerId, String shortCode, int maxClicks, Duration ttl) {
    this.id = UUID.randomUUID();
    this.createdAt = LocalDateTime.now();
    this.originalUrl = originalUrl;
    this.ownerId = ownerId;
    this.shortCode = shortCode;
    this.maxClicks = maxClicks;
    this.clickCount = 0;
    this.ttl = ttl;
    this.active = true;
  }

  public ShortLink(
      UUID id,
      String originalUrl,
      UUID ownerId,
      String shortCode,
      int clickCount,
      int maxClicks,
      LocalDateTime createdAt,
      Duration ttl,
      boolean active) {
    this.id = id;
    this.createdAt = createdAt;
    this.originalUrl = originalUrl;
    this.ownerId = ownerId;
    this.shortCode = shortCode;
    this.clickCount = clickCount;
    this.maxClicks = maxClicks;
    this.ttl = ttl;
    this.active = active;
  }

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(createdAt.plus(ttl));
  }

  public boolean canBeAccessed() {
    return active && !isExpired() && clickCount < maxClicks;
  }

  public void incrementClickCount() {
    this.clickCount++;
    if (clickCount >= maxClicks) {
      this.active = false;
    }
  }

  public void deactivate() {
    this.active = false;
  }

  public UUID getId() {
    return id;
  }

  public String getOriginalUrl() {
    return originalUrl;
  }

  public UUID getOwnerId() {
    return ownerId;
  }

  public String getShortCode() {
    return shortCode;
  }

  public int getClickCount() {
    return clickCount;
  }

  public int getMaxClicks() {
    return maxClicks;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public Duration getTtl() {
    return ttl;
  }

  public boolean isActive() {
    return active;
  }
}
