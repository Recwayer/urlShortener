package unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.example.domain.User;
import ru.example.repository.UserRepository;
import ru.example.service.UserService;
import ru.example.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock private UserRepository repository;

  private UserService service;

  @BeforeEach
  void setUp() {
    service = new UserServiceImpl(repository);
  }

  @Test
  void createNewUser_CreatesAndSavesUser() {
    User user = new User();
    when(repository.save(any(User.class))).thenReturn(user);

    User result = service.createNewUser();

    assertNotNull(result);
    assertNotNull(result.getId());
    verify(repository).save(any(User.class));
  }

  @Test
  void getOrCreateUser_ExistingUser_ReturnsUser() {
    UUID userId = UUID.randomUUID();
    User existingUser = new User(userId, java.time.LocalDateTime.now());
    when(repository.getOrCreate(userId)).thenReturn(existingUser);

    User result = service.getOrCreateUser(userId);

    assertEquals(existingUser, result);
    verify(repository).getOrCreate(userId);
  }

  @Test
  void getUser_ExistingUser_ReturnsUser() {
    UUID userId = UUID.randomUUID();
    User user = new User(userId, java.time.LocalDateTime.now());
    when(repository.findById(userId)).thenReturn(Optional.of(user));

    Optional<User> result = service.getUser(userId);

    assertTrue(result.isPresent());
    assertEquals(user, result.get());
  }

  @Test
  void getUser_NonExistingUser_ReturnsEmpty() {
    UUID userId = UUID.randomUUID();
    when(repository.findById(userId)).thenReturn(Optional.empty());

    Optional<User> result = service.getUser(userId);

    assertFalse(result.isPresent());
  }
}
