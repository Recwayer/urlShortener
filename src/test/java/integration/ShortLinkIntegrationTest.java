package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.example.domain.ShortLink;
import ru.example.repository.LinkRepository;
import ru.example.repository.UserRepository;
import ru.example.repository.impl.ShortLinkRepositoryImpl;
import ru.example.repository.impl.UserRepositoryImpl;
import ru.example.service.LinkService;
import ru.example.service.Notifier;
import ru.example.service.UserService;
import ru.example.service.impl.NotificationService;
import ru.example.service.impl.ShortLinkServiceImpl;
import ru.example.service.impl.UserServiceImpl;

class ShortLinkIntegrationTest {

  private LinkService shortLinkService;
  private UUID userId;

  @BeforeEach
  void setUp() {
    LinkRepository linkRepository = new ShortLinkRepositoryImpl();
    UserRepository userRepository = new UserRepositoryImpl();
    Notifier notifier = new NotificationService();

    shortLinkService = new ShortLinkServiceImpl(linkRepository, notifier);
    UserService userService = new UserServiceImpl(userRepository);

    userId = userService.createNewUser().getId();
  }

  @Test
  void createAndRedirect_Integration_Success() {
    ShortLink link = shortLinkService.createShortLink("http://example.com", userId, 3);

    Optional<String> result1 = shortLinkService.redirect(link.getShortCode());
    Optional<String> result2 = shortLinkService.redirect(link.getShortCode());
    Optional<String> result3 = shortLinkService.redirect(link.getShortCode());
    Optional<String> result4 = shortLinkService.redirect(link.getShortCode());

    assertTrue(result1.isPresent());
    assertTrue(result2.isPresent());
    assertTrue(result3.isPresent());
    assertFalse(result4.isPresent());

    List<ShortLink> userLinks = shortLinkService.getUserLinks(userId);
    assertEquals(1, userLinks.size());
    assertEquals(3, userLinks.get(0).getClickCount());
  }

  @Test
  void multipleLinksPerUser_Integration_Success() {
    shortLinkService.createShortLink("http://example.com", userId, 5);
    shortLinkService.createShortLink("http://google.com", userId, 5);
    shortLinkService.createShortLink("http://github.com", userId, 5);

    List<ShortLink> userLinks = shortLinkService.getUserLinks(userId);

    assertEquals(3, userLinks.size());
    assertTrue(
        userLinks.stream().anyMatch(link -> link.getOriginalUrl().equals("http://example.com")));
    assertTrue(
        userLinks.stream().anyMatch(link -> link.getOriginalUrl().equals("http://google.com")));
    assertTrue(
        userLinks.stream().anyMatch(link -> link.getOriginalUrl().equals("http://github.com")));
  }
}
