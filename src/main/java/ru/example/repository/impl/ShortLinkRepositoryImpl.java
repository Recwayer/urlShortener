package ru.example.repository.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import ru.example.domain.ShortLink;
import ru.example.repository.LinkRepository;

public class ShortLinkRepositoryImpl implements LinkRepository {
  private final Map<UUID, ShortLink> linksById = new ConcurrentHashMap<>();
  private final Map<String, ShortLink> linksByShortCode = new ConcurrentHashMap<>();
  private final Map<UUID, List<ShortLink>> linksByUser = new ConcurrentHashMap<>();

  public void save(ShortLink link) {
    ShortLink existingLink = linksById.get(link.getId());
    if (existingLink != null) {
      removeFromUserLinks(existingLink);
    }

    linksById.put(link.getId(), link);
    linksByShortCode.put(link.getShortCode(), link);
    linksByUser.computeIfAbsent(link.getOwnerId(), k -> new ArrayList<>()).add(link);
  }

  public Optional<ShortLink> findById(UUID linkId) {
    return Optional.ofNullable(linksById.get(linkId));
  }

  public Optional<ShortLink> findByShortCode(String shortCode) {
    return Optional.ofNullable(linksByShortCode.get(shortCode));
  }

  public List<ShortLink> findByUserId(UUID userId) {
    return linksByUser.getOrDefault(userId, Collections.emptyList());
  }

  public boolean delete(UUID linkId, UUID userId) {
    ShortLink link = linksById.get(linkId);
    if (link != null && link.getOwnerId().equals(userId)) {
      return removeLink(link);
    }
    return false;
  }

  public List<ShortLink> findAll() {
    return new ArrayList<>(linksById.values());
  }

  private boolean removeLink(ShortLink link) {
    linksById.remove(link.getId());
    linksByShortCode.remove(link.getShortCode());
    removeFromUserLinks(link);
    return true;
  }

  private void removeFromUserLinks(ShortLink link) {
    List<ShortLink> userLinks = linksByUser.get(link.getOwnerId());
    if (userLinks != null) {
      userLinks.removeIf(l -> l.getId().equals(link.getId()));
      if (userLinks.isEmpty()) {
        linksByUser.remove(link.getOwnerId());
      }
    }
  }
}
