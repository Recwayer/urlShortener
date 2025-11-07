package unit.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.example.domain.User;
import ru.example.repository.UserRepository;
import ru.example.repository.impl.UserRepositoryImpl;

class UserRepositoryImplTest {

  private UserRepository repository;

  @BeforeEach
  void setUp() {
    repository = new UserRepositoryImpl();
  }

  @Test
  void saveAndFindById_ReturnsSavedUser() {
    User user = new User();

    repository.save(user);
    Optional<User> found = repository.findById(user.getId());

    assertTrue(found.isPresent());
    assertEquals(user.getId(), found.get().getId());
  }

  @Test
  void getOrCreate_ExistingUser_ReturnsUser() {
    User user = new User();
    repository.save(user);

    User result = repository.getOrCreate(user.getId());

    assertEquals(user.getId(), result.getId());
  }

  @Test
  void getOrCreate_NewUser_CreatesAndReturnsUser() {
    UUID newUserId = UUID.randomUUID();

    User result = repository.getOrCreate(newUserId);

    assertNotNull(result);
    assertEquals(newUserId, result.getId());

    Optional<User> found = repository.findById(newUserId);
    assertTrue(found.isPresent());
  }

  @Test
  void findById_NonExistingUser_ReturnsEmpty() {
    Optional<User> found = repository.findById(UUID.randomUUID());
    assertFalse(found.isPresent());
  }
}
