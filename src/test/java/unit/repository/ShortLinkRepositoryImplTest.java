package unit.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.example.domain.ShortLink;
import ru.example.repository.LinkRepository;
import ru.example.repository.impl.ShortLinkRepositoryImpl;

class ShortLinkRepositoryImplTest {

  private LinkRepository repository;
  private UUID userId1;
  private UUID userId2;

  @BeforeEach
  void setUp() {
    repository = new ShortLinkRepositoryImpl();
    userId1 = UUID.randomUUID();
    userId2 = UUID.randomUUID();
  }

  @Test
  void saveAndFindByShortCode_ReturnsSavedLink() {
    ShortLink link =
        new ShortLink("http://example.com", userId1, "abc123", 10, Duration.ofHours(1));

    repository.save(link);
    Optional<ShortLink> found = repository.findByShortCode("abc123");

    assertTrue(found.isPresent());
    assertEquals(link.getId(), found.get().getId());
  }

  @Test
  void findByUserId_ReturnsOnlyUserLinks() {
    ShortLink link1 =
        new ShortLink("http://example.com", userId1, "abc123", 10, Duration.ofHours(1));
    ShortLink link2 =
        new ShortLink("http://google.com", userId2, "def456", 10, Duration.ofHours(1));

    repository.save(link1);
    repository.save(link2);

    List<ShortLink> user1Links = repository.findByUserId(userId1);
    List<ShortLink> user2Links = repository.findByUserId(userId2);

    assertEquals(1, user1Links.size());
    assertEquals(1, user2Links.size());
    assertEquals("abc123", user1Links.get(0).getShortCode());
    assertEquals("def456", user2Links.get(0).getShortCode());
  }

  @Test
  void delete_UserOwnsLink_ReturnsTrue() {
    ShortLink link =
        new ShortLink("http://example.com", userId1, "abc123", 10, Duration.ofHours(1));
    repository.save(link);

    boolean deleted = repository.delete(link.getId(), userId1);

    assertTrue(deleted);
    assertFalse(repository.findByShortCode("abc123").isPresent());
  }

  @Test
  void delete_UserDoesNotOwnLink_ReturnsFalse() {
    ShortLink link =
        new ShortLink("http://example.com", userId1, "abc123", 10, Duration.ofHours(1));
    repository.save(link);

    boolean deleted = repository.delete(link.getId(), userId2);

    assertFalse(deleted);
    assertTrue(repository.findByShortCode("abc123").isPresent());
  }
}
