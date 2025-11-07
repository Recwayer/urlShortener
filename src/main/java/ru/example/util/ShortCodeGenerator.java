package ru.example.util;

import java.util.Random;
import java.util.UUID;

public class ShortCodeGenerator {
  private static final String RANDOM_CHAR = "0123456789abcdefghijklmnopqrstuvwxyz";

  private ShortCodeGenerator() {}

  public static String generateShortCode(String originalUrl, UUID userId) {
    String input = originalUrl + userId.toString() + System.nanoTime() + UUID.randomUUID();
    String shortCode = Integer.toHexString(input.hashCode());
    if (shortCode.length() < 8) {
      StringBuilder result = new StringBuilder(shortCode);
      Random random = new Random();
      while (result.length() < 8) {
        char randomHexChar = RANDOM_CHAR.charAt(random.nextInt(RANDOM_CHAR.length()));
        result.append(randomHexChar);
      }
      shortCode = result.toString();
    }
    return shortCode;
  }
}
