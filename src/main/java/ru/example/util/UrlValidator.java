package ru.example.util;

public class UrlValidator {
  private static final int MAX_LENGTH_URL = 2048;

  private UrlValidator() {}

  public static boolean isValidUrl(String url) {
    if (url == null || url.trim().isEmpty()) {
      return false;
    }

    String trimmedUrl = url.trim();
    return trimmedUrl.startsWith("http://") || trimmedUrl.startsWith("https://");
  }

  public static String validateAndPrepare(String url) {
    if (url == null) {
      throw new IllegalArgumentException("URL-адрес не может быть null");
    }

    String trimmed = url.trim();

    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("URL не может быть пустым");
    }

    if (trimmed.length() > MAX_LENGTH_URL) {
      throw new IllegalArgumentException("URL слишком длинный");
    }

    if (!isValidUrl(trimmed)) {
      throw new IllegalArgumentException("URL должен начинаться с http:// или https://");
    }

    return trimmed;
  }
}
