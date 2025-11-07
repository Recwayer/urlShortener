package unit.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import ru.example.util.ShortCodeGenerator;

class ShortCodeGeneratorTest {

  @Test
  void generateShortCode_AlwaysReturns8Characters() {
    String code = ShortCodeGenerator.generateShortCode("http://example.com", UUID.randomUUID());
    assertEquals(8, code.length());
  }

  @Test
  void generateShortCode_DifferentInputs_ProduceDifferentCodes() {
    UUID userId = UUID.randomUUID();
    String code1 = ShortCodeGenerator.generateShortCode("http://example.com", userId);
    String code2 = ShortCodeGenerator.generateShortCode("http://google.com", userId);

    assertNotEquals(code1, code2);
  }

  @Test
  void generateShortCode_DifferentUsers_ProduceDifferentCodes() {
    UUID userId1 = UUID.randomUUID();
    UUID userId2 = UUID.randomUUID();
    String url = "http://example.com";

    String code1 = ShortCodeGenerator.generateShortCode(url, userId1);
    String code2 = ShortCodeGenerator.generateShortCode(url, userId2);

    assertNotEquals(code1, code2);
  }

  @Test
  void generateShortCode_ValidCharacters() {
    String code = ShortCodeGenerator.generateShortCode("http://example.com", UUID.randomUUID());
    assertTrue(code.matches("[0-9a-z]{8}"));
  }
}
