package ru.example.config;

import static ru.example.util.UrlValidator.isValidUrl;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Properties;

public class AppConfig {
  private static final Properties PROPERTIES = new Properties();

  private static final String DEFAULT_BASE_URL = "http://localhost:8080/";
  private static final String DEFAULT_TTL = "PT24H";
  private static final int DEFAULT_MAX_CLICKS = 100;
  private static final long DEFAULT_CLEANUP_INTERVAL = 3600000L;
  private static final long MIN_CLEANUP_INTERVAL = 1000L;
  private static final int MIN_MAX_CLICKS = 1;

  static {
    loadConfiguration();
    validateConfig();
  }

  private static void loadConfiguration() {
    try (InputStream input =
        AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
      if (input == null) {
        System.out.println("Файл конфигурации не найден, используются настройки по умолчанию.");
        setDefaults();
      } else {
        PROPERTIES.load(input);
        System.out.println("Конфигурация успешно загружена из application.properties");
      }
    } catch (IOException e) {
      System.out.println(
          "Ошибка загрузки конфигурации, используются значения по умолчанию: " + e.getMessage());
      setDefaults();
    }
  }

  private static void setDefaults() {
    PROPERTIES.setProperty("app.short-link.base-url", DEFAULT_BASE_URL);
    PROPERTIES.setProperty("app.short-link.default-ttl", DEFAULT_TTL);
    PROPERTIES.setProperty("app.short-link.default-max-clicks", String.valueOf(DEFAULT_MAX_CLICKS));
    PROPERTIES.setProperty("app.cleanup.interval-ms", String.valueOf(DEFAULT_CLEANUP_INTERVAL));
  }

  private static void validateConfig() {
    validateBaseUrl();
    validateTtl();
    validateMaxClicks();
    validateCleanupInterval();
  }

  private static void validateBaseUrl() {
    String baseUrl = PROPERTIES.getProperty("app.short-link.base-url");
    if (baseUrl == null || baseUrl.trim().isEmpty()) {
      System.out.println(
          "Ошибка: app.short-link.base-url не может быть пустым. Используется значение по умолчанию.");
      PROPERTIES.setProperty("app.short-link.base-url", DEFAULT_BASE_URL);
      return;
    }

    baseUrl = baseUrl.trim();
    if (!isValidUrl(baseUrl)) {
      System.out.println(
          "Ошибка: app.short-link.base-url имеет невалидный формат: "
              + baseUrl
              + ". Используется значение по умолчанию.");
      PROPERTIES.setProperty("app.short-link.base-url", DEFAULT_BASE_URL);
    }
  }

  private static void validateTtl() {
    String ttlString = PROPERTIES.getProperty("app.short-link.default-ttl");
    if (ttlString == null || ttlString.trim().isEmpty()) {
      System.out.println(
          "Ошибка: app.short-link.default-ttl не может быть пустым. Используется значение по умолчанию.");
      PROPERTIES.setProperty("app.short-link.default-ttl", DEFAULT_TTL);
      return;
    }

    try {
      Duration duration = Duration.parse(ttlString.trim());
      if (duration.isNegative() || duration.isZero()) {
        System.out.println(
            "Ошибка: app.short-link.default-ttl должен быть положительным. Используется значение по умолчанию.");
        PROPERTIES.setProperty("app.short-link.default-ttl", DEFAULT_TTL);
      }
    } catch (DateTimeParseException e) {
      System.out.println(
          "Ошибка: неверный формат app.short-link.default-ttl: "
              + ttlString
              + ". Используется значение по умолчанию. Ошибка: "
              + e.getMessage());
      PROPERTIES.setProperty("app.short-link.default-ttl", DEFAULT_TTL);
    }
  }

  private static void validateMaxClicks() {
    String maxClicksString = PROPERTIES.getProperty("app.short-link.default-max-clicks");
    if (maxClicksString == null || maxClicksString.trim().isEmpty()) {
      System.out.println(
          "Ошибка: app.short-link.default-max-clicks не может быть пустым. Используется значение по умолчанию.");
      PROPERTIES.setProperty(
          "app.short-link.default-max-clicks", String.valueOf(DEFAULT_MAX_CLICKS));
      return;
    }

    try {
      int maxClicks = Integer.parseInt(maxClicksString.trim());
      if (maxClicks < MIN_MAX_CLICKS) {
        System.out.println(
            "Ошибка: app.short-link.default-max-clicks должен быть не менее "
                + MIN_MAX_CLICKS
                + ". Используется значение по умолчанию.");
        PROPERTIES.setProperty(
            "app.short-link.default-max-clicks", String.valueOf(DEFAULT_MAX_CLICKS));
      }
    } catch (NumberFormatException e) {
      System.out.println(
          "Ошибка: неверный формат app.short-link.default-max-clicks: "
              + maxClicksString
              + ". Используется значение по умолчанию. Ошибка: "
              + e.getMessage());
      PROPERTIES.setProperty(
          "app.short-link.default-max-clicks", String.valueOf(DEFAULT_MAX_CLICKS));
    }
  }

  private static void validateCleanupInterval() {
    String intervalString = PROPERTIES.getProperty("app.cleanup.interval-ms");
    if (intervalString == null || intervalString.trim().isEmpty()) {
      System.out.println(
          "Ошибка: app.cleanup.interval-ms не может быть пустым. Используется значение по умолчанию.");
      PROPERTIES.setProperty("app.cleanup.interval-ms", String.valueOf(DEFAULT_CLEANUP_INTERVAL));
      return;
    }

    try {
      long interval = Long.parseLong(intervalString.trim());
      if (interval < MIN_CLEANUP_INTERVAL) {
        System.out.println(
            "Ошибка: app.cleanup.interval-ms должен быть не менее "
                + MIN_CLEANUP_INTERVAL
                + " мс. Используется значение по умолчанию.");
        PROPERTIES.setProperty("app.cleanup.interval-ms", String.valueOf(DEFAULT_CLEANUP_INTERVAL));
      }
    } catch (NumberFormatException e) {
      System.out.println(
          "Ошибка: неверный формат app.cleanup.interval-ms: "
              + intervalString
              + ". Используется значение по умолчанию. Ошибка: "
              + e.getMessage());
      PROPERTIES.setProperty("app.cleanup.interval-ms", String.valueOf(DEFAULT_CLEANUP_INTERVAL));
    }
  }

  public static String getBaseUrl() {
    String baseUrl = PROPERTIES.getProperty("app.short-link.base-url", DEFAULT_BASE_URL);
    if (!baseUrl.endsWith("/")) {
      baseUrl += "/";
    }
    return baseUrl;
  }

  public static Duration getDefaultTtl() {
    try {
      return Duration.parse(PROPERTIES.getProperty("app.short-link.default-ttl", DEFAULT_TTL));
    } catch (DateTimeParseException e) {
      System.out.println(
          "Критическая ошибка: неверный формат TTL, используется значение по умолчанию.");
      return Duration.parse(DEFAULT_TTL);
    }
  }

  public static int getDefaultMaxClicks() {
    try {
      int maxClicks =
          Integer.parseInt(
              PROPERTIES.getProperty(
                  "app.short-link.default-max-clicks", String.valueOf(DEFAULT_MAX_CLICKS)));
      return Math.max(maxClicks, MIN_MAX_CLICKS);
    } catch (NumberFormatException e) {
      System.out.println(
          "Критическая ошибка: неверный формат max-clicks, используется значение по умолчанию.");
      return DEFAULT_MAX_CLICKS;
    }
  }

  public static long getCleanupInterval() {
    try {
      long interval =
          Long.parseLong(
              PROPERTIES.getProperty(
                  "app.cleanup.interval-ms", String.valueOf(DEFAULT_CLEANUP_INTERVAL)));
      return Math.max(interval, MIN_CLEANUP_INTERVAL);
    } catch (NumberFormatException e) {
      System.out.println(
          "Критическая ошибка: неверный формат cleanup-interval, используется значение по умолчанию.");
      return DEFAULT_CLEANUP_INTERVAL;
    }
  }
}
