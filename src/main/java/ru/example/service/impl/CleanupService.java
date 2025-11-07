package ru.example.service.impl;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import ru.example.config.AppConfig;
import ru.example.domain.ShortLink;
import ru.example.repository.LinkRepository;
import ru.example.service.Notifier;

public class CleanupService {
  private final LinkRepository repository;
  private final Notifier notifier;
  private final Timer timer;

  public CleanupService(LinkRepository repository, Notifier notifier) {
    this.repository = repository;
    this.notifier = notifier;
    this.timer = new Timer(true);
  }

  public void start() {
    long interval = AppConfig.getCleanupInterval();
    timer.scheduleAtFixedRate(new CleanupTask(), interval, interval);
    System.out.println("Сервис очистки запущен с интервалом: " + interval + " мс");
  }

  public void stop() {
    timer.cancel();
    System.out.println("Сервис очистки остановлен");
  }

  private class CleanupTask extends TimerTask {
    @Override
    public void run() {
      List<ShortLink> allLinks = repository.findAll();
      int expiredCount = 0;

      for (ShortLink link : allLinks) {
        if (link.isExpired()) {
          repository.delete(link.getId(), link.getOwnerId());
          expiredCount++;
          notifier.notifyLinkExpired(link);
        }
      }

      if (expiredCount > 0) {
        System.out.printf("Очистка: удалено %d устаревших ссылок%n", expiredCount);
      }
    }
  }
}
