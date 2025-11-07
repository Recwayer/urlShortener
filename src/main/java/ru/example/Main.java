package ru.example;

import ru.example.controller.ConsoleController;
import ru.example.repository.impl.ShortLinkRepositoryImpl;
import ru.example.repository.impl.UserRepositoryImpl;
import ru.example.service.LinkService;
import ru.example.service.Notifier;
import ru.example.service.UserService;
import ru.example.service.impl.CleanupService;
import ru.example.service.impl.NotificationService;
import ru.example.service.impl.ShortLinkServiceImpl;
import ru.example.service.impl.UserServiceImpl;

public class Main {
  public static void main(String[] args) {
    try {
      ShortLinkRepositoryImpl linkRepository = new ShortLinkRepositoryImpl();
      UserRepositoryImpl userRepositoryImpl = new UserRepositoryImpl();
      Notifier notifier = new NotificationService();

      LinkService shortLinkService = new ShortLinkServiceImpl(linkRepository, notifier);
      UserService userServiceImpl = new UserServiceImpl(userRepositoryImpl);
      CleanupService cleanupService = new CleanupService(linkRepository, notifier);

      ConsoleController controller =
          new ConsoleController(shortLinkService, userServiceImpl, notifier, cleanupService);

      controller.start();

    } catch (Exception e) {
      System.err.println("Критическая ошибка при запуске приложения: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
