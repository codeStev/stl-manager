package dev.codestev.server.business.importing.gateways;

import dev.codestev.server.business.importing.ModelImportServiceImpl;
import dev.codestev.server.persistence.ModelRepository;
import dev.codestev.server.persistence.StlFileRepository;
import dev.codestev.server.persistence.model.Model;
import dev.codestev.server.persistence.model.ModelVariant;
import dev.codestev.server.persistence.model.StlFile;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StlGatewayJpa implements ModelImportServiceImpl.StlFileGateway {
    private final StlFileRepository stlFileRepository;
    private final ModelRepository modelRepository;
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(StlGatewayJpa.class);
    private final EntityManager entityManager;

    StlGatewayJpa(StlFileRepository stlFileRepository, ModelRepository modelRepository, EntityManager entityManager) {
        this.stlFileRepository = stlFileRepository;
        this.modelRepository = modelRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void upsertStlFile(
            long modelId,
            Long variantIdOrNull,
            String fileName,
            String relativePath,
            long size,
            long lastModifiedMillis,
            String sha256OrNull
    ) {
        // Look up by model + storagePath to decide update vs insert
        StlFile found = stlFileRepository.findByModels_IdAndStoragePathIgnoreCase(modelId, relativePath).orElse(null);

        if (found == null) {
            // Create new file
            Model model = modelRepository.findByIdBasic(modelId)
                    .orElseThrow(() -> new IllegalArgumentException("Model not found: " + modelId));

            // Load variants separately if needed
            ModelVariant variant = null;
            if (variantIdOrNull != null) {
                List<ModelVariant> variants = modelRepository.findVariantsByModelId(modelId);
                variant = variants.stream()
                        .filter(v -> v.getId().equals(variantIdOrNull))
                        .findFirst()
                        .orElse(null);
            }

            StlFile file = new StlFile();
            file.setFileName(fileName);
            file.setStoragePath(relativePath);
            file.setSizeBytes(size);
            file.setChecksumSha256(sha256OrNull);
            file.addModel(model);

            if (variant != null) {
                variant.addStlFile(file);
                // Since variant is loaded separately, we need to save it explicitly
                entityManager.merge(variant);
            }

            stlFileRepository.save(file);
            log.debug("Inserted STL {} for model {}", relativePath, modelId);

        } else {
            // Update existing
            boolean changed = false;

            if (found.getSizeBytes() != size) {
                found.setSizeBytes(size);
                changed = true;
            }
            if ((sha256OrNull != null && !sha256OrNull.equals(found.getChecksumSha256()))
                    || (sha256OrNull == null && found.getChecksumSha256() != null)) {
                found.setChecksumSha256(sha256OrNull);
                changed = true;
            }

            if (changed) {
                // Handle variant linkage for updates
                if (variantIdOrNull != null) {
                    List<ModelVariant> variants = modelRepository.findVariantsByModelId(modelId);
                    ModelVariant variant = variants.stream()
                            .filter(v -> v.getId().equals(variantIdOrNull))
                            .findFirst()
                            .orElse(null);

                    if (variant != null) {
                        variant.addStlFile(found);
                        entityManager.merge(variant);
                    }
                }

                stlFileRepository.save(found);
                log.debug("Updated STL {} for model {}", relativePath, modelId);
            }
        }
    }

    @Override
    @Transactional
    public void deleteStlFile(long stlId) {
        stlFileRepository.deleteById(stlId);
    }
}