package ru.example.service.impl;

import java.time.Duration;
import ru.example.domain.ShortLink;
import ru.example.service.Notifier;

public class NotificationService implements Notifier {

  public void notifyLinkAccessed(ShortLink link) {
    System.out.printf(
        "Ссылка %s открыта. Переходы: %d/%d%n",
        link.getShortCode(), link.getClickCount(), link.getMaxClicks());
  }

  public void notifyClicksExhausted(ShortLink link) {
    System.out.printf(
        "Ссылка %s достигла лимита переходов (%d). Рекомендуется создать новую ссылку.%n",
        link.getShortCode(), link.getMaxClicks());
  }

  public void notifyLinkExpired(ShortLink link) {
    System.out.printf(
        "%nСрок действия ссылки %s истек. Рекомендуется создать новую ссылку.%n",
        link.getShortCode());
  }

  public void notifyLinkCreated(ShortLink link, String fullShortUrl) {
    System.out.printf("Создана короткая ссылка: %s%n", fullShortUrl);
    System.out.printf("Короткий код ссылки: %s%n", link.getShortCode());
    System.out.printf("Оригинал: %s%n", link.getOriginalUrl());
    System.out.printf(
        "Максимум переходов: %d, Срок действия истекает через: %s%n",
        link.getMaxClicks(), formatDuration(link.getTtl()));
  }

  public void notifyLinkDeleted(String shortCode) {
    System.out.printf("Ссылка %s удалена%n", shortCode);
  }

  private String formatDuration(Duration duration) {
    if (duration.toDays() > 0) {
      return duration.toDays() + " дней";
    } else if (duration.toHours() > 0) {
      return duration.toHours() + " часов";
    } else if (duration.toMinutes() > 0) {
      return duration.toMinutes() + " минут";
    } else {
      return duration.getSeconds() + " секунд";
    }
  }
}
