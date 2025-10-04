package dev.codestev.server.persistence;

import dev.codestev.server.persistence.model.ModelVariant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelVariantRepository extends JpaRepository<ModelVariant, Long> {
}
