package dev.codestev.server.persistence;

import dev.codestev.server.persistence.model.StlFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StlFileRepository extends JpaRepository<StlFile, Long> {

    List<StlFile> findAllByModels_Id(Long modelId);
    boolean deleteAllByModels_Id(Long modelId);
    Optional<StlFile> findByModels_IdAndStoragePathIgnoreCase(Long modelId, String storagePath);
}
