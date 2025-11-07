package ru.example.service.impl;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ru.example.config.AppConfig;
import ru.example.domain.ShortLink;
import ru.example.repository.LinkRepository;
import ru.example.service.LinkService;
import ru.example.service.Notifier;
import ru.example.util.ShortCodeGenerator;
import ru.example.util.UrlValidator;

public class ShortLinkServiceImpl implements LinkService {
  private final LinkRepository repository;
  private final Notifier notifier;

  public ShortLinkServiceImpl(LinkRepository repository, Notifier notifier) {
    this.repository = repository;
    this.notifier = notifier;
  }

  public ShortLink createShortLink(String originalUrl, UUID userId, Integer maxClicks) {
    String validatedUrl = UrlValidator.validateAndPrepare(originalUrl);
    String shortCode = ShortCodeGenerator.generateShortCode(validatedUrl, userId);
    int actualMaxClicks = maxClicks != null ? maxClicks : AppConfig.getDefaultMaxClicks();
    Duration systemTtl = AppConfig.getDefaultTtl();

    ShortLink link = new ShortLink(validatedUrl, userId, shortCode, actualMaxClicks, systemTtl);
    repository.save(link);

    return link;
  }

  public Optional<String> redirect(String shortCode) {
    Optional<ShortLink> linkOpt = repository.findByShortCode(shortCode);

    if (linkOpt.isEmpty()) {
      return Optional.empty();
    }

    ShortLink link = linkOpt.get();

    if (!link.canBeAccessed()) {
      if (link.isExpired()) {
        notifier.notifyLinkExpired(link);
      } else if (link.getClickCount() >= link.getMaxClicks()) {
        notifier.notifyClicksExhausted(link);
      }
      return Optional.empty();
    }

    link.incrementClickCount();
    notifier.notifyLinkAccessed(link);

    return Optional.of(link.getOriginalUrl());
  }

  public List<ShortLink> getUserLinks(UUID userId) {
    return repository.findByUserId(userId);
  }

  public boolean deleteLink(UUID linkId, UUID userId) {
    return repository.delete(linkId, userId);
  }

  public Optional<ShortLink> updateLinkMaxClicks(UUID linkId, UUID userId, int newMaxClicks) {
    Optional<ShortLink> linkOpt = repository.findById(linkId);

    if (linkOpt.isPresent()) {
      ShortLink link = linkOpt.get();
      if (link.getOwnerId().equals(userId)) {
        ShortLink updatedLink =
            new ShortLink(
                link.getId(),
                link.getOriginalUrl(),
                link.getOwnerId(),
                link.getShortCode(),
                link.getClickCount(),
                newMaxClicks,
                link.getCreatedAt(),
                link.getTtl(),
                true);
        repository.save(updatedLink);
        return Optional.of(updatedLink);
      }
    }
    return Optional.empty();
  }

  public Optional<ShortLink> findLinkById(UUID linkId) {
    return repository.findById(linkId);
  }
}
