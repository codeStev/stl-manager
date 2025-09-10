package dev.codestev.server.persistence;

import dev.codestev.server.persistence.model.Library;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LibraryRepository extends JpaRepository<Library, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByPathIgnoreCase(String name);
    Optional<Library> findByNameIgnoreCase(String name);
    Optional<Library> findByPath(String path);
}

