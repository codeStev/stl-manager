package dev.codestev.server.business.service;

import dev.codestev.server.business.mapping.ModelMapper;
import dev.codestev.server.business.model.ModelDetailed;
import dev.codestev.server.persistence.ModelRepository;
import dev.codestev.server.persistence.model.Model;
import dev.codestev.server.persistence.model.ModelPreview;
import dev.codestev.server.persistence.model.ModelVariant;
import dev.codestev.server.persistence.model.StlFile;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ModelService {

    private final ModelRepository modelRepository;
    private final ModelMapper modelMapper;

    public ModelService(ModelRepository modelRepository, ModelMapper modelMapper) {
        this.modelRepository = modelRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional(readOnly = true)
    public List<ModelDetailed> getAllModelDetailsByLibraryId(long libraryId) {
        // Step 1: Load basic model info
        List<Model> models = modelRepository.findAllByLibrary_IdBasic(libraryId);

        if (models.isEmpty()) {
            return List.of();
        }

        // Create a map for easy lookup
        Map<Long, Model> modelMap = models.stream()
                .collect(Collectors.toMap(Model::getId, m -> m));

        // Step 2: Load and group variants
        List<ModelVariant> variants = modelRepository.findVariantsByLibraryId(libraryId);
        Map<Long, List<ModelVariant>> variantsByModel = variants.stream()
                .collect(Collectors.groupingBy(v -> v.getModel().getId()));

        for (Map.Entry<Long, List<ModelVariant>> entry : variantsByModel.entrySet()) {
            Model model = modelMap.get(entry.getKey());
            if (model != null) {
                model.setVariants(entry.getValue()); // List<ModelVariant>
            }
        }

        // Step 3: Load and group previews
        List<ModelPreview> previews = modelRepository.findPreviewsByLibraryId(libraryId);
        Map<Long, List<ModelPreview>> previewsByModel = previews.stream()
                .collect(Collectors.groupingBy(p -> p.getModel().getId()));

        for (Map.Entry<Long, List<ModelPreview>> entry : previewsByModel.entrySet()) {
            Model model = modelMap.get(entry.getKey());
            if (model != null) {
                model.setPreviews(entry.getValue()); // List<ModelPreview>
            }
        }

        // Step 4: Load and group STL files
        List<StlFile> stlFiles = modelRepository.findStlFilesByLibraryId(libraryId);
        Map<Long, Set<StlFile>> stlFilesByModel = stlFiles.stream()
                .flatMap(sf -> sf.getModels().stream()
                        .filter(m -> m.getLibrary().getId().equals(libraryId))
                        .map(m -> new AbstractMap.SimpleEntry<>(m.getId(), sf)))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toSet())
                )); // Changed to toSet() since stlFiles is a Set

        for (Map.Entry<Long, Set<StlFile>> entry : stlFilesByModel.entrySet()) {
            Model model = modelMap.get(entry.getKey());
            if (model != null) {
                model.setStlFiles(entry.getValue()); // Set<StlFile>
            }
        }

        return models.stream()
                .map(modelMapper::toBusiness)
                .toList();
    }

    @Transactional(readOnly = true)
    public ModelDetailed getModelDetailedById(long id) {
        // Load model with basic associations
        Model model = modelRepository.findByIdBasic(id)
                .orElseThrow(() -> new EntityNotFoundException("Model not found: " + id));

        // Load variants
        List<ModelVariant> variants = modelRepository.findVariantsByModelId(id);
        model.setVariants(variants);

        // Load previews
        List<ModelPreview> previews = modelRepository.findPreviewsByModelId(id);
        model.setPreviews(previews);

        // Load STL files
        List<StlFile> stlFiles = modelRepository.findStlFilesByModelId(id);
        model.setStlFiles(new LinkedHashSet<>(stlFiles));

        return modelMapper.toBusiness(model);
    }
}