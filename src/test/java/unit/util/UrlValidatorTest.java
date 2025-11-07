package unit.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import ru.example.util.UrlValidator;

class UrlValidatorTest {

  @Test
  void isValidUrl_ValidHttpUrl_ReturnsTrue() {
    assertTrue(UrlValidator.isValidUrl("http://example.com"));
  }

  @Test
  void isValidUrl_ValidHttpsUrl_ReturnsTrue() {
    assertTrue(UrlValidator.isValidUrl("https://example.com"));
  }

  @Test
  void isValidUrl_InvalidProtocol_ReturnsFalse() {
    assertFalse(UrlValidator.isValidUrl("ftp://example.com"));
    assertFalse(UrlValidator.isValidUrl("file:///etc/passwd"));
  }

  @Test
  void isValidUrl_NullOrEmpty_ReturnsFalse() {
    assertFalse(UrlValidator.isValidUrl(null));
    assertFalse(UrlValidator.isValidUrl(""));
    assertFalse(UrlValidator.isValidUrl("   "));
  }

  @Test
  void validateAndPrepare_ValidUrl_ReturnsTrimmed() {
    String result = UrlValidator.validateAndPrepare("  https://example.com  ");
    assertEquals("https://example.com", result);
  }

  @Test
  void validateAndPrepare_InvalidUrl_ThrowsException() {
    assertThrows(
        IllegalArgumentException.class, () -> UrlValidator.validateAndPrepare("invalid-url"));
  }

  @Test
  void validateAndPrepare_TooLongUrl_ThrowsException() {
    String longUrl = "http://example.com/" + "a".repeat(3000);
    assertThrows(IllegalArgumentException.class, () -> UrlValidator.validateAndPrepare(longUrl));
  }
}
