package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
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

class MultiUserIntegrationTest {

  private LinkService shortLinkService;
  private UUID user1Id, user2Id;

  @BeforeEach
  void setUp() {
    LinkRepository linkRepository = new ShortLinkRepositoryImpl();
    UserRepository userRepository = new UserRepositoryImpl();
    Notifier notificationService = new NotificationService();

    shortLinkService = new ShortLinkServiceImpl(linkRepository, notificationService);
    UserService userService = new UserServiceImpl(userRepository);

    user1Id = userService.createNewUser().getId();
    user2Id = userService.createNewUser().getId();
  }

  @Test
  void differentUsersSameUrl_DifferentShortCodes() {
    String sameUrl = "http://example.com";

    ShortLink link1 = shortLinkService.createShortLink(sameUrl, user1Id, 10);
    ShortLink link2 = shortLinkService.createShortLink(sameUrl, user2Id, 10);

    assertNotEquals(link1.getShortCode(), link2.getShortCode());

    List<ShortLink> user1Links = shortLinkService.getUserLinks(user1Id);
    List<ShortLink> user2Links = shortLinkService.getUserLinks(user2Id);

    assertEquals(1, user1Links.size());
    assertEquals(1, user2Links.size());
    assertEquals(link1.getShortCode(), user1Links.get(0).getShortCode());
    assertEquals(link2.getShortCode(), user2Links.get(0).getShortCode());
  }

  @Test
  void userIsolation_OneUserCannotAccessOthersLinks() {
    shortLinkService.createShortLink("http://user1.com", user1Id, 10);
    shortLinkService.createShortLink("http://user2.com", user2Id, 10);

    List<ShortLink> user1Links = shortLinkService.getUserLinks(user1Id);
    assertEquals(1, user1Links.size());
    assertEquals("http://user1.com", user1Links.get(0).getOriginalUrl());

    List<ShortLink> user2Links = shortLinkService.getUserLinks(user2Id);
    assertEquals(1, user2Links.size());
    assertEquals("http://user2.com", user2Links.get(0).getOriginalUrl());
  }

  @Test
  void userCannotDeleteOthersLinks() {
    ShortLink user1Link = shortLinkService.createShortLink("http://user1.com", user1Id, 10);

    boolean deleted = shortLinkService.deleteLink(user1Link.getId(), user2Id);

    assertFalse(deleted);

    List<ShortLink> user1Links = shortLinkService.getUserLinks(user1Id);
    assertEquals(1, user1Links.size());
  }
}
