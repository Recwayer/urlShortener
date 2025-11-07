package ru.example.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ru.example.domain.ShortLink;

public interface LinkRepository {
  void save(ShortLink link);

  Optional<ShortLink> findById(UUID linkId);

  Optional<ShortLink> findByShortCode(String shortCode);

  List<ShortLink> findByUserId(UUID userId);

  boolean delete(UUID linkId, UUID userId);

  List<ShortLink> findAll();
}
