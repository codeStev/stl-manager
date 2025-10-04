package dev.codestev.server.business.importing.gateways;

import dev.codestev.server.business.importing.ModelImportServiceImpl;
import dev.codestev.server.persistence.ArtistRepository;
import dev.codestev.server.persistence.LibraryRepository;
import dev.codestev.server.persistence.ModelRepository;
import dev.codestev.server.persistence.StlFileRepository;
import dev.codestev.server.persistence.model.Artist;
import dev.codestev.server.persistence.model.Library;
import dev.codestev.server.persistence.model.Model;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ModelGatewayJpa implements ModelImportServiceImpl.ModelGateway {

    private final ModelRepository modelRepository;
    private final StlFileRepository stlFileRepository;
    private final ArtistRepository artistRepository;

    ModelGatewayJpa(
            LibraryRepository libraryRepository,
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
                stls.put(
                        f.getStoragePath(),
                        new ModelImportServiceImpl.ExistingStl(
                                f.getId(),
                                f.getStoragePath(),
                                f.getSizeBytes(),
                                f.getChecksumSha256()
                        )
                );
            }
            Long artistId = m.getArtist().getId();
            Map<String, ModelImportServiceImpl.ExistingVariant> modelVariants = m.getVariants().stream().map(x -> new ModelImportServiceImpl.ExistingVariant(x.getId(), x.getName())).collect(Collectors.toMap(ModelImportServiceImpl.ExistingVariant::name, v -> v));
            map.put(key, new ModelImportServiceImpl.ExistingModel(m.getId(), m.getName(),artistId, modelVariants, stls));
        }
        return map;
    }

    @Override
    @Transactional
    public long createModel(String name, Library library, Long artistId) {
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

        modelRepository.save(model);
    }

    @Override
    @Transactional
    public void deleteModel(long modelId) {
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