
package dev.codestev.server.persistence;

import dev.codestev.server.persistence.model.Library;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LibraryRepository extends JpaRepository<Library, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByPathIgnoreCase(String name);

    @Query("SELECT DISTINCT l FROM Library l LEFT JOIN FETCH l.models WHERE l.path = :path")
    Optional<Library> findByPath(@Param("path") String path);

    // For listing libraries without models (performance optimization)
    @Query("SELECT l FROM Library l")
    List<Library> findAllBasicInfo();

    // For getting single library without models
    @Query("SELECT l FROM Library l WHERE l.id = :id")
    Optional<Library> findByIdBasicInfo(@Param("id") Long id);

    // For getting library with models when needed
    @Query("SELECT DISTINCT l FROM Library l LEFT JOIN FETCH l.models WHERE l.id = :id")
    Optional<Library> findByIdWithModels(@Param("id") Long id);
}