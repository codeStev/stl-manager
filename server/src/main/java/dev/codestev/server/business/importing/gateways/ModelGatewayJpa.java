package dev.codestev.server.business.importing.gateways;

import dev.codestev.server.business.importing.ModelImportServiceImpl;
import dev.codestev.server.persistence.ArtistRepository;
import dev.codestev.server.persistence.ModelRepository;
import dev.codestev.server.persistence.StlFileRepository;
import dev.codestev.server.persistence.model.Artist;
import dev.codestev.server.persistence.model.Library;
import dev.codestev.server.persistence.model.Model;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ModelGatewayJpa implements ModelImportServiceImpl.ModelGateway {

    private final ModelRepository modelRepository;
    private final StlFileRepository stlFileRepository;
    private final ArtistRepository artistRepository;

    ModelGatewayJpa(
            ModelRepository modelRepository,
            StlFileRepository stlFileRepository,
            ArtistRepository artistRepository
    ) {
        this.modelRepository = modelRepository;
        this.stlFileRepository = stlFileRepository;
        this.artistRepository = artistRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, ModelImportServiceImpl.ExistingModel> loadAllModelsAsMapByNameAndLibrary(long libraryId) {
        Map<String, ModelImportServiceImpl.ExistingModel> map = new LinkedHashMap<>();

        for (var m : modelRepository.findAllByLibrary_Id(libraryId)) {
            String key = composeKey(m.getArtist(), m.getName());

            Map<String, ModelImportServiceImpl.ExistingStl> stls = new LinkedHashMap<>();
            for (var f : stlFileRepository.findAllByModels_Id(m.getId())) {
                stls.putIfAbsent(
                        f.getStoragePath(),
                        new ModelImportServiceImpl.ExistingStl(
                                f.getId(),
                                f.getStoragePath(),
                                f.getSizeBytes(),
                                f.getChecksumSha256()
                        )
                );
            }

            Long artistId = (m.getArtist() != null) ? m.getArtist().getId() : null;

            Map<String, ModelImportServiceImpl.ExistingVariant> modelVariants = new LinkedHashMap<>();
            if (m.getVariants() != null) {
                for (var x : m.getVariants()) {
                    modelVariants.putIfAbsent(x.getName(), new ModelImportServiceImpl.ExistingVariant(x.getId(), x.getName()));
                }
            }

            map.putIfAbsent(key, new ModelImportServiceImpl.ExistingModel(m.getId(), m.getName(), artistId, modelVariants, stls));
        }
        return map;
    }

    @Override
    @Transactional
    public long getOrCreateModel(String name, Library library, Long artistId) {

        Model foundModel = modelRepository.findByNameAndLibraryAndArtist_Id(name, library, artistId).orElse(null);

        if (foundModel != null) {
            return foundModel.getId();
        }
            var model = new Model();
            model.setName(name);
            model.setLibrary(library);
            if (artistId != null) {
                var artistRef = artistRepository.getReferenceById(artistId);
                model.setArtist(artistRef);
            } else {
                model.setArtist(null);
            }
            return modelRepository.save(model).getId();
    }

    @Override
    @Transactional
    public void updateModelArtist(long modelId, Long artistId) {
        var model = modelRepository.findById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("Model not found: " + modelId));
        if (artistId == null) {
            model.setArtist(null);
        } else {
            var artistRef = artistRepository.getReferenceById(artistId);
            model.setArtist(artistRef);
        }
        // No explicit save needed; the managed entity will be flushed at transaction commit.
    }

    @Override
    @Transactional
    public void deleteModel(long modelId) {
        // WARNING: ensure this deletes only associations, not shared STL entities.
        stlFileRepository.deleteAllByModels_Id(modelId);
        modelRepository.deleteById(modelId);
    }

    private static String composeKey(Artist artist, String modelName) {
        if (artist != null && artist.getName() != null && !artist.getName().isBlank()) {
            return artist.getName() + "/" + modelName;
        }
        return modelName;
    }
}