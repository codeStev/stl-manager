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
import java.util.List;
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
        // This fetches artist and library eagerly — no proxies created for them
        List<Model> models = modelRepository.findAllWithArtistAndLibraryByLibrary_Id(libraryId);

        Map<String, ModelImportServiceImpl.ExistingModel> result =
                new LinkedHashMap<>(Math.max(16, models.size() * 2));

        for (Model m : models) {
            Artist artist = m.getArtist();
            String key = composeKey(artist, m.getName());

            ModelImportServiceImpl.ExistingModel view = new ModelImportServiceImpl.ExistingModel(
                    m.getId(),
                    m.getName(),
                    artist != null ? artist.getId() : null,
                    Map.of(), // variantsByName
                    Map.of()  // stlByPath
            );
            result.put(key, view);
        }
        return result;
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
            // Use basic query for artist - we only need the reference
            Artist artist = artistRepository.findByIdBasic(artistId)
                    .orElseThrow(() -> new IllegalArgumentException("Artist not found: " + artistId));
            model.setArtist(artist);
        } else {
            model.setArtist(null);
        }
        return modelRepository.save(model).getId();
    }

    @Override
    @Transactional
    public long createModel(String name, Library library, Long artistId){
        var model = new Model();
        model.setName(name);
        model.setLibrary(library);
        if (artistId != null) {
            // Use basic query for artist
            Artist artist = artistRepository.findByIdBasic(artistId)
                    .orElseThrow(() -> new IllegalArgumentException("Artist not found: " + artistId));
            model.setArtist(artist);
        } else {
            model.setArtist(null);
        }
        return modelRepository.save(model).getId();
    }

    @Override
    @Transactional
    public void updateModelArtist(long modelId, Long artistId) {
        // Use basic model query since we only need to update the artist reference
        var model = modelRepository.findByIdBasic(modelId)
                .orElseThrow(() -> new IllegalArgumentException("Model not found: " + modelId));
        if (artistId == null) {
            model.setArtist(null);
        } else {
            Artist artist = artistRepository.findByIdBasic(artistId)
                    .orElseThrow(() -> new IllegalArgumentException("Artist not found: " + artistId));
            model.setArtist(artist);
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