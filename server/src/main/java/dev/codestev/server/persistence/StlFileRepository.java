package dev.codestev.server.persistence;

import dev.codestev.server.persistence.model.StlFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StlFileRepository extends JpaRepository<StlFile, Long> {

    @Modifying
    @Query("DELETE FROM StlFile s WHERE s.id IN (SELECT sf.id FROM StlFile sf JOIN sf.models m WHERE m.id = :modelId)")
    void deleteAllByModels_Id(@Param("modelId") Long modelId);

    @Query("SELECT DISTINCT s FROM StlFile s LEFT JOIN FETCH s.models m WHERE m.id = :modelId AND LOWER(s.storagePath) = LOWER(:storagePath)")
    Optional<StlFile> findByModels_IdAndStoragePathIgnoreCase(@Param("modelId") Long modelId, @Param("storagePath") String storagePath);

    // Override findById with models loaded
    @Override
    @Query("SELECT DISTINCT s FROM StlFile s LEFT JOIN FETCH s.models WHERE s.id = :id")
    Optional<StlFile> findById(@Param("id") Long id);

    // Override findAll with models loaded
    @Override
    @Query("SELECT DISTINCT s FROM StlFile s LEFT JOIN FETCH s.models")
    List<StlFile> findAll();

    // Basic StlFile without models for simple operations
    @Query("SELECT s FROM StlFile s WHERE s.id = :id")
    Optional<StlFile> findByIdBasic(@Param("id") Long id);

    // All StlFiles without models for listing
    @Query("SELECT s FROM StlFile s")
    List<StlFile> findAllBasic();
}