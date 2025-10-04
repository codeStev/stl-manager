package dev.codestev.server.persistence;

import dev.codestev.server.persistence.model.Model;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModelRepository extends JpaRepository<Model, Long> {

    Optional<Model> findModelById(Long id);
    List<Model> findAllByLibrary_Id(Long libraryId);

}
