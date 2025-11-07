package ru.example.controller;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import ru.example.config.AppConfig;
import ru.example.domain.ShortLink;
import ru.example.domain.User;
import ru.example.service.LinkService;
import ru.example.service.Notifier;
import ru.example.service.UserService;
import ru.example.service.impl.CleanupService;

public class ConsoleController {
  private final LinkService shortLinkService;
  private final UserService userServiceImpl;
  private final Notifier notifier;
  private final CleanupService cleanupService;
  private User currentUser;
  private final Scanner scanner;

  public ConsoleController(
      LinkService shortLinkService,
      UserService userServiceImpl,
      Notifier notifier,
      CleanupService cleanupService) {
    this.shortLinkService = shortLinkService;
    this.userServiceImpl = userServiceImpl;
    this.notifier = notifier;
    this.cleanupService = cleanupService;
    this.scanner = new Scanner(System.in);
  }

  public void start() {
    initializeUser();
    cleanupService.start();

    System.out.println("Сервис сокращения ссылок запущен!");

    while (true) {
      showMenu();
      String choice = scanner.nextLine().trim();

      try {
        switch (choice) {
          case "1":
            createShortLink();
            break;
          case "2":
            redirectToLink();
            break;
          case "3":
            showMyLinks();
            break;
          case "4":
            editLink();
            break;
          case "5":
            deleteLink();
            break;
          case "6":
            switchUser();
            break;
          case "0":
            System.out.println("Выход!");
            scanner.close();
            cleanupService.stop();
            return;
          default:
            System.out.println("Неверный выбор. Попробуйте снова.");
        }
      } catch (Exception e) {
        System.out.println("Ошибка: " + e.getMessage());
      }
    }
  }

  private void initializeUser() {
    System.out.println("Введите ваш User ID (или нажмите Enter для создания нового):");
    String input = scanner.nextLine().trim();

    if (input.isEmpty()) {
      currentUser = userServiceImpl.createNewUser();
      System.out.println("Создан новый пользователь: " + currentUser.getId());
    } else {
      try {
        UUID userId = UUID.fromString(input);
        currentUser = userServiceImpl.getOrCreateUser(userId);
        System.out.println("Пользователь восстановлен: " + currentUser.getId());
      } catch (IllegalArgumentException e) {
        System.out.println("Неверный формат UUID. Создаю нового пользователя.");
        currentUser = userServiceImpl.createNewUser();
      }
    }
  }

  private void showMenu() {
    System.out.println("Текущий пользователь: " + currentUser.getId());
    System.out.println("1. Создать короткую ссылку");
    System.out.println("2. Перейти по короткой ссылке");
    System.out.println("3. Мои ссылки");
    System.out.println("4. Редактировать ссылку");
    System.out.println("5. Удалить ссылку");
    System.out.println("6. Сменить пользователя");
    System.out.println("0. Выход");
    System.out.print("Выберите действие: ");
  }

  private void createShortLink() {
    System.out.print("Введите длинную ссылку (должна начинаться с http:// или https://): ");
    String originalUrl = scanner.nextLine().trim();

    System.out.print(
        "Максимальное количество переходов (по умолчанию "
            + AppConfig.getDefaultMaxClicks()
            + "): ");
    String maxClicksInput = scanner.nextLine().trim();

    Integer maxClicks = null;
    if (!maxClicksInput.isEmpty()) {
      try {
        maxClicks = Integer.parseInt(maxClicksInput);
        if (maxClicks <= 0) {
          System.out.println("Количество переходов должно быть положительным числом");
          return;
        }
      } catch (NumberFormatException e) {
        System.out.println("Неверный формат числа");
        return;
      }
    }

    try {
      ShortLink link =
          shortLinkService.createShortLink(originalUrl, currentUser.getId(), maxClicks);
      String fullShortUrl = AppConfig.getBaseUrl() + link.getShortCode();
      notifier.notifyLinkCreated(link, fullShortUrl);
    } catch (Exception e) {
      System.out.println("Ошибка при создании ссылки: " + e.getMessage());
    }
  }

  private void redirectToLink() {
    System.out.print("Введите короткий код ссылки: ");
    String shortCode = scanner.nextLine().trim();

    try {
      var originalUrlOpt = shortLinkService.redirect(shortCode);

      if (originalUrlOpt.isPresent()) {
        String originalUrl = originalUrlOpt.get();
        System.out.println("Перенаправление на: " + originalUrl);

        if (Desktop.isDesktopSupported()
            && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
          Desktop.getDesktop().browse(new URI(originalUrl));
        } else {
          System.out.println("Автоматическое открытие браузера не поддерживается");
          System.out.println("Откройте ссылку вручную: " + originalUrl);
        }
      } else {
        System.out.println("Ссылка не найдена или недоступна");
      }
    } catch (Exception e) {
      System.out.println("Ошибка при переходе по ссылке: " + e.getMessage());
    }
  }

  private void showMyLinks() {
    List<ShortLink> links = shortLinkService.getUserLinks(currentUser.getId());

    if (links.isEmpty()) {
      System.out.println("У вас пока нет созданных ссылок");
      return;
    }

    System.out.println("\n Ваши ссылки:");
    for (int i = 0; i < links.size(); i++) {
      ShortLink link = links.get(i);
      String status = link.canBeAccessed() ? "Активна" : "Неактивна";
      if (link.isExpired()) {
        status = "Истекла";
      } else if (link.getClickCount() >= link.getMaxClicks()) {
        status = "Лимит";
      }

      System.out.printf("%d. %s%s%n", i + 1, AppConfig.getBaseUrl(), link.getShortCode());
      System.out.printf("   -> %s%n", link.getOriginalUrl());
      System.out.printf(
          "   Переходы: %d/%d | Создана: %s | Статус: %s%n",
          link.getClickCount(), link.getMaxClicks(), link.getCreatedAt().toString(), status);
    }
  }

  private void editLink() {
    showMyLinks();
    List<ShortLink> links = shortLinkService.getUserLinks(currentUser.getId());

    if (links.isEmpty()) {
      return;
    }

    System.out.print("Выберите номер ссылки для редактирования: ");
    try {
      int index = Integer.parseInt(scanner.nextLine().trim()) - 1;
      if (index < 0 || index >= links.size()) {
        System.out.println("Неверный номер");
        return;
      }

      ShortLink link = links.get(index);
      System.out.print(
          "Новое максимальное количество переходов (текущее: " + link.getMaxClicks() + "): ");
      int newMaxClicks = Integer.parseInt(scanner.nextLine().trim());

      if (newMaxClicks <= 0) {
        System.out.println("Количество переходов должно быть положительным числом");
        return;
      }

      if (newMaxClicks < link.getClickCount()) {
        System.out.println(
            "Новый лимит не может быть меньше текущего количества переходов ("
                + link.getClickCount()
                + ")");
        return;
      }

      var updatedLink =
          shortLinkService.updateLinkMaxClicks(link.getId(), currentUser.getId(), newMaxClicks);
      if (updatedLink.isPresent()) {
        System.out.println("Лимит переходов обновлен");
      } else {
        System.out.println("Не удалось обновить ссылку");
      }
    } catch (NumberFormatException e) {
      System.out.println("Неверный формат числа");
    }
  }

  private void deleteLink() {
    showMyLinks();
    List<ShortLink> links = shortLinkService.getUserLinks(currentUser.getId());

    if (links.isEmpty()) {
      return;
    }

    System.out.print("Выберите номер ссылки для удаления: ");
    try {
      int index = Integer.parseInt(scanner.nextLine().trim()) - 1;
      if (index < 0 || index >= links.size()) {
        System.out.println("Неверный номер");
        return;
      }

      ShortLink link = links.get(index);
      System.out.print(
          "Вы уверены, что хотите удалить ссылку " + link.getShortCode() + "? (y/N): ");
      String confirmation = scanner.nextLine().trim();

      if (confirmation.equalsIgnoreCase("y")) {
        boolean deleted = shortLinkService.deleteLink(link.getId(), currentUser.getId());
        if (deleted) {
          notifier.notifyLinkDeleted(link.getShortCode());
        } else {
          System.out.println("Не удалось удалить ссылку");
        }
      }
    } catch (NumberFormatException e) {
      System.out.println("Неверный формат числа");
    }
  }

  private void switchUser() {
    System.out.print("Введите User ID (или нажмите Enter для создания нового): ");
    String input = scanner.nextLine().trim();

    if (input.isEmpty()) {
      currentUser = userServiceImpl.createNewUser();
      System.out.println("Создан новый пользователь: " + currentUser.getId());
    } else {
      try {
        UUID userId = UUID.fromString(input);
        currentUser = userServiceImpl.getOrCreateUser(userId);
        System.out.println("Переключен на пользователя: " + currentUser.getId());
      } catch (IllegalArgumentException e) {
        System.out.println("Неверный формат UUID");
      }
    }
  }
}
