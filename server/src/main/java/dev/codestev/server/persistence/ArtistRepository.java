
package dev.codestev.server.persistence;

import dev.codestev.server.persistence.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArtistRepository extends JpaRepository<Artist, Long> {

    // Basic artist info without models
    @Query("SELECT a FROM Artist a WHERE a.id = :id")
    Optional<Artist> findByIdBasic(@Param("id") Long id);

    // Basic artist info without models
    @Query("SELECT a FROM Artist a WHERE a.name = :name")
    Optional<Artist> findByName(@Param("name") String name);

    // Artist with all models loaded
    @Query("SELECT DISTINCT a FROM Artist a LEFT JOIN FETCH a.models WHERE a.id = :id")
    Optional<Artist> findByIdWithModels(@Param("id") Long id);

    // All artists without models for listing
    @Query("SELECT a FROM Artist a")
    List<Artist> findAllBasic();

    // Override default methods with basic queries
    @Override
    @Query("SELECT a FROM Artist a WHERE a.id = :id")
    Optional<Artist> findById(@Param("id") Long id);

    @Override
    @Query("SELECT a FROM Artist a")
    List<Artist> findAll();
}