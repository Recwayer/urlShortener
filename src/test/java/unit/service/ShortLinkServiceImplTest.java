package unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.example.domain.ShortLink;
import ru.example.repository.LinkRepository;
import ru.example.service.Notifier;
import ru.example.service.impl.ShortLinkServiceImpl;

@ExtendWith(MockitoExtension.class)
class ShortLinkServiceImplTest {

  @Mock private LinkRepository repository;

  @Mock private Notifier notifier;

  private ShortLinkServiceImpl service;
  private UUID userId;

  @BeforeEach
  void setUp() {
    service = new ShortLinkServiceImpl(repository, notifier);
    userId = UUID.randomUUID();
  }

  @Test
  void createShortLink_ValidUrl_CreatesAndSavesLink() {
    doNothing().when(repository).save(any(ShortLink.class));

    ShortLink link = service.createShortLink("http://example.com", userId, 50);

    assertNotNull(link);
    assertEquals("http://example.com", link.getOriginalUrl());
    assertEquals(userId, link.getOwnerId());
    assertEquals(50, link.getMaxClicks());
    verify(repository).save(any(ShortLink.class));
  }

  @Test
  void redirect_MaxClicksReached_ReturnsEmpty() {
    ShortLink link = new ShortLink("http://example.com", userId, "abc123", 1, Duration.ofHours(1));
    link.incrementClickCount();
    when(repository.findByShortCode("abc123")).thenReturn(Optional.of(link));

    Optional<String> result = service.redirect("abc123");

    assertFalse(result.isPresent());
    verify(notifier).notifyClicksExhausted(link);
    verify(repository, never()).save(any(ShortLink.class));
  }

  @Test
  void redirect_ExpiredLink_ReturnsEmpty() {
    ShortLink link =
        new ShortLink(
            UUID.randomUUID(),
            "http://example.com",
            userId,
            "abc123",
            0,
            10,
            java.time.LocalDateTime.now().minusHours(2),
            Duration.ofHours(1),
            true);
    when(repository.findByShortCode("abc123")).thenReturn(Optional.of(link));

    Optional<String> result = service.redirect("abc123");

    assertFalse(result.isPresent());
    verify(notifier).notifyLinkExpired(link);
    verify(repository, never()).save(any(ShortLink.class));
  }

  @Test
  void redirect_LinkNotFound_ReturnsEmpty() {
    when(repository.findByShortCode("nonexistent")).thenReturn(Optional.empty());

    Optional<String> result = service.redirect("nonexistent");

    assertFalse(result.isPresent());
    verify(notifier, never()).notifyLinkAccessed(any());
    verify(repository, never()).save(any(ShortLink.class));
  }

  @Test
  void updateLinkMaxClicks_UserOwnsLink_UpdatesSuccessfully() {
    ShortLink originalLink =
        new ShortLink("http://example.com", userId, "abc123", 5, Duration.ofHours(1));
    when(repository.findById(originalLink.getId())).thenReturn(Optional.of(originalLink));
    doNothing().when(repository).save(any(ShortLink.class));

    Optional<ShortLink> result = service.updateLinkMaxClicks(originalLink.getId(), userId, 10);

    assertTrue(result.isPresent());
    assertEquals(10, result.get().getMaxClicks());
    verify(repository).save(any(ShortLink.class));
  }

  @Test
  void updateLinkMaxClicks_UserDoesNotOwnLink_ReturnsEmpty() {
    UUID otherUserId = UUID.randomUUID();
    ShortLink originalLink =
        new ShortLink("http://example.com", otherUserId, "abc123", 5, Duration.ofHours(1));
    when(repository.findById(originalLink.getId())).thenReturn(Optional.of(originalLink));

    Optional<ShortLink> result = service.updateLinkMaxClicks(originalLink.getId(), userId, 10);

    assertFalse(result.isPresent());
    verify(repository, never()).save(any(ShortLink.class));
  }

  @Test
  void updateLinkMaxClicks_LinkNotFound_ReturnsEmpty() {
    UUID linkId = UUID.randomUUID();
    when(repository.findById(linkId)).thenReturn(Optional.empty());

    Optional<ShortLink> result = service.updateLinkMaxClicks(linkId, userId, 10);

    assertFalse(result.isPresent());
    verify(repository, never()).save(any(ShortLink.class));
  }

  @Test
  void deleteLink_UserOwnsLink_ReturnsTrue() {
    ShortLink link = new ShortLink("http://example.com", userId, "abc123", 5, Duration.ofHours(1));
    when(repository.delete(link.getId(), userId)).thenReturn(true);

    boolean result = service.deleteLink(link.getId(), userId);

    assertTrue(result);
    verify(repository).delete(link.getId(), userId);
  }

  @Test
  void deleteLink_UserDoesNotOwnLink_ReturnsFalse() {
    UUID otherUserId = UUID.randomUUID();
    ShortLink link =
        new ShortLink("http://example.com", otherUserId, "abc123", 5, Duration.ofHours(1));
    when(repository.delete(link.getId(), userId)).thenReturn(false);

    boolean result = service.deleteLink(link.getId(), userId);

    assertFalse(result);
    verify(repository).delete(link.getId(), userId);
  }

  @Test
  void getUserLinks_ReturnsUserLinks() {
    when(repository.findByUserId(userId))
        .thenReturn(
            java.util.List.of(
                new ShortLink("http://example.com", userId, "abc123", 5, Duration.ofHours(1)),
                new ShortLink("http://google.com", userId, "def456", 10, Duration.ofHours(2))));

    var links = service.getUserLinks(userId);

    assertEquals(2, links.size());
    verify(repository).findByUserId(userId);
  }
}
