package ru.example.service;

import ru.example.domain.ShortLink;

public interface Notifier {
  void notifyLinkAccessed(ShortLink link);

  void notifyClicksExhausted(ShortLink link);

  void notifyLinkExpired(ShortLink link);

  void notifyLinkCreated(ShortLink link, String shortUrl);

  void notifyLinkDeleted(String shortCode);
}
