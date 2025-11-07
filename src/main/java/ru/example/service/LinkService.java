package ru.example.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ru.example.domain.ShortLink;

public interface LinkService {
  ShortLink createShortLink(String originalUrl, UUID userId, Integer maxClicks);

  Optional<String> redirect(String shortCode);

  List<ShortLink> getUserLinks(UUID userId);

  boolean deleteLink(UUID linkId, UUID userId);

  Optional<ShortLink> updateLinkMaxClicks(UUID linkId, UUID userId, int newMaxClicks);

  Optional<ShortLink> findLinkById(UUID linkId);
}
