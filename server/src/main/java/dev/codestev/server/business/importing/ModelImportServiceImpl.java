package dev.codestev.server.business.importing;

import dev.codestev.server.persistence.model.Artist;
import dev.codestev.server.persistence.model.Library;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class ModelImportServiceImpl implements ModelImportService {
    private static final Logger log = LoggerFactory.getLogger(ModelImportServiceImpl.class);

    private final ModelImportProperties props;
    private final FilesystemModelScanner scanner;

    private final LibraryGateway libraryGateway;
    private final ModelGateway modelGateway;
    private final StlFileGateway stlGateway;
    private final ArtistGateway artistGateway;
    private final VariantGateway variantGateway;

    private final ReentrantLock runLock = new ReentrantLock();

    public ModelImportServiceImpl(
            ModelImportProperties props,
            FilesystemModelScanner scanner,
            LibraryGateway libraryGateway,
            ModelGateway modelGateway,
            StlFileGateway stlGateway,
            ArtistGateway artistGateway,
            VariantGateway variantGateway
    ) {
        this.props = Objects.requireNonNull(props, "props");
        this.scanner = Objects.requireNonNull(scanner, "scanner");
        this.libraryGateway = Objects.requireNonNull(libraryGateway, "libraryGateway");
        this.modelGateway = Objects.requireNonNull(modelGateway, "modelGateway");
        this.stlGateway = Objects.requireNonNull(stlGateway, "stlGateway");
        this.artistGateway = Objects.requireNonNull(artistGateway, "artistGateway");
        this.variantGateway = Objects.requireNonNull(variantGateway, "variantGateway");
    }

    @Override
    @Transactional
    public void runFullSync() {
        if (!runLock.tryLock()) {
            log.info("Model import already running, skipping");
            return;
        }

        try {
            List<Library> libraries = libraryGateway.getAllLibraries();
            if (libraries.isEmpty()) {
                log.warn("Model import skipped: no libraries exist");
                return;
            }

            for (Library library : libraries) {
                Path root = Path.of(library.getPath()).toAbsolutePath().normalize();
                log.info("Starting model import from {}", root);

                Map<String, FilesystemModelScanner.ScannedModel> scanned;
                try {
                    scanned = scanner.scan(root, props.isComputeHashes());
                } catch (IOException e) {
                    throw new RuntimeException("Model scan failed for " + root, e);
                }

                // Fetch current DB state (business view)
                Map<String, ExistingModel> existing = modelGateway.loadAllModelsAsMapByNameAndLibrary(library.getId());

                // Create or update
                for (var entry : scanned.entrySet()) {
                    String modelKey = entry.getKey(); // composite "Artist/Model" from scanner
                    var scannedModel = entry.getValue();

                    if (!existing.containsKey(modelKey)) {
                        createModel(scannedModel, library);
                    } else {
                        updateModel(existing.get(modelKey), scannedModel);
                    }
                }

                // Delete orphans (models not found on disk)
                if (props.isDeleteOrphans()) {
                    Set<String> scannedNames = scanned.keySet();
                    for (var orphan : existing.values()) {
                        Artist artist = artistGateway.findById(orphan.artistId());
                        if (!scannedNames.contains(artist != null ? artist.getName() + "/" + orphan.name() : orphan.name())) {
                            deleteModel(orphan);
                        }
                    }
                } else {
                    log.info("Orphan deletion disabled; any models missing on disk are retained in DB");
                }

                log.info("Model import finished for {}. Scanned models: {}, Existing (before): {}",
                        library.getName(), scanned.size(), existing.size());
            }
        } finally {
            runLock.unlock();
        }
    }

    private void createModel(FilesystemModelScanner.ScannedModel scannedModel, Library library) {
        log.info("Creating model '{}'", scannedModel.name());

        // Ensure artist exists and attach to model
        Long artistId = null;
        if (scannedModel.artist() != null && !scannedModel.artist().isBlank()) {
            artistId = artistGateway.getOrCreateArtist(scannedModel.artist());
        }

        long modelId = modelGateway.createModel(scannedModel.model(), library, artistId);

        // Prepare variant ids for any named variants
        Map<String, Long> variantIds = new HashMap<>();
        for (var f : scannedModel.files()) {
            Long variantId = null;
            if (f.variant().isPresent() && !f.variant().get().isBlank()) {
                String variantName = f.variant().get();
                variantId = variantIds.computeIfAbsent(variantName, v -> variantGateway.upsertVariant(modelId, v));
            }
            stlGateway.upsertStlFile(
                    modelId,
                    variantId,
                    f.fileName(),
                    f.relativePath(),
                    f.size(),
                    f.lastModifiedMillis(),
                    f.sha256().orElse(null)
            );
        }
    }

    private void updateModel(ExistingModel existing, FilesystemModelScanner.ScannedModel scannedModel) {
        // 1) Ensure/Update artist association
        Long desiredArtistId = null;
        if (scannedModel.artist() != null && !scannedModel.artist().isBlank()) {
            desiredArtistId = artistGateway.getOrCreateArtist(scannedModel.artist());
        }

        if (!Objects.equals(existing.artistId(), desiredArtistId)) {
            Long previousArtistId = existing.artistId();
            modelGateway.updateModelArtist(existing.id(), desiredArtistId);
            if (props.isDeleteOrphans() && previousArtistId != null) {
                artistGateway.deleteArtistIfOrphan(previousArtistId);
            }
        }

        // 2) Upsert variants present in scan, collect ids
        Set<String> scannedVariantNames = new HashSet<>();
        for (var f : scannedModel.files()) {
            f.variant().filter(v -> !v.isBlank()).ifPresent(scannedVariantNames::add);
        }

        Map<String, Long> variantIds = new HashMap<>();
        for (String vName : scannedVariantNames) {
            long vId = variantGateway.upsertVariant(existing.id(), vName);
            variantIds.put(vName, vId);
        }

        // 3) Diff STL files by relative path (includes variant path when present)
        Map<String, ExistingStl> existingByPath = new HashMap<>(existing.stlByPath());

        for (var f : scannedModel.files()) {
            var ex = existingByPath.remove(f.relativePath());
            Long variantId = f.variant()
                    .filter(v -> !v.isBlank())
                    .map(variantIds::get)
                    .orElse(null);

            if (ex == null) {
                // New file
                stlGateway.upsertStlFile(existing.id(), variantId, f.fileName(), f.relativePath(), f.size(), f.lastModifiedMillis(), f.sha256().orElse(null));
            } else {
                // Detect change and update if needed
                boolean changed = ex.size() != f.size()
                        || (f.sha256().isPresent() && !Objects.equals(ex.sha256(), f.sha256().get()));
                if (changed) {
                    stlGateway.upsertStlFile(existing.id(), variantId, f.fileName(), f.relativePath(), f.size(), f.lastModifiedMillis(), f.sha256().orElse(null));
                }
            }
        }

        // Remaining existing files that were not seen in scan -> delete
        if (!existingByPath.isEmpty()) {
            for (var ex : existingByPath.values()) {
                stlGateway.deleteStlFile(ex.id());
            }
        }

        // 4) Delete variants that are no longer present (optional)
        if (props.isDeleteOrphans() && existing.variantsByName() != null && !existing.variantsByName().isEmpty()) {
            for (var entry : existing.variantsByName().entrySet()) {
                String vName = entry.getKey();
                var variant = entry.getValue();
                if (!scannedVariantNames.contains(vName)) {
                    // Implementation should ensure safe delete (only when no files reference the variant)
                    variantGateway.deleteVariant(variant.id());
                }
            }
        }
    }

    private void deleteModel(ExistingModel orphan) {
        log.info("Deleting model '{}'", orphan.name());
        Long artistId = orphan.artistId();
        modelGateway.deleteModel(orphan.id());
        if (props.isDeleteOrphans() && artistId != null) {
            artistGateway.deleteArtistIfOrphan(artistId);
        }
    }

    public interface LibraryGateway {
        List<Library> getAllLibraries();
    }

    // Business gateway abstractions to decouple service from persistence.
    public interface ModelGateway {
        // Keyed by "ArtistName/ModelName" when artist is present, otherwise just "ModelName"
        Map<String, ExistingModel> loadAllModelsAsMapByNameAndLibrary(long libraryId);

        // Create model with optional artist ownership
        long createModel(String name, Library library, Long artistId);

        // Update artist association on an existing model (can be set to null)
        void updateModelArtist(long modelId, Long artistId);

        void deleteModel(long modelId);
    }

    public interface StlFileGateway {
        // Variant can be null for files at the model root
        void upsertStlFile(long modelId,
                           Long variantIdOrNull,
                           String fileName,
                           String relativePath,
                           long size,
                           long lastModifiedMillis,
                           String sha256OrNull);

        void deleteStlFile(long stlId);
    }

    public interface ArtistGateway {
        Long getOrCreateArtist(String name);
        void deleteArtistIfOrphan(long artistId);
        Artist findById(long artistId);
    }

    public interface VariantGateway {
        long upsertVariant(long modelId, String name);
        void deleteVariant(long variantId);
    }

    // Lightweight business views
    public record ExistingModel(
            long id,
            String name,
            Long artistId,
            Map<String, ExistingVariant> variantsByName,
            Map<String, ExistingStl> stlByPath
    ) {}

    public record ExistingVariant(long id, String name) {}

    public record ExistingStl(long id, String relativePath, long size, String sha256) {}
}