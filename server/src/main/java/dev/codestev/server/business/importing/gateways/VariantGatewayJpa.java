package dev.codestev.server.business.importing.gateways;

import dev.codestev.server.business.importing.ModelImportServiceImpl;
import dev.codestev.server.persistence.ModelRepository;
import dev.codestev.server.persistence.ModelVariantRepository;
import dev.codestev.server.persistence.model.ModelVariant;
import org.springframework.stereotype.Component;

@Component
public class VariantGatewayJpa implements ModelImportServiceImpl.VariantGateway {

    
    private final ModelVariantRepository variantRepository;
    private final ModelRepository modelRepository;

    public VariantGatewayJpa(ModelVariantRepository variantRepository, ModelRepository modelRepository) {
        this.variantRepository = variantRepository;
        this.modelRepository = modelRepository;
    }


    @Override
    public long upsertVariant(long modelId, String name) {
        var model = modelRepository.findById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("Model not found: " + modelId));
        var existingVariant = model.getVariants().stream()
                .filter(v -> v.getName().equals(name))
                .findFirst()
                .orElse(null);

        if (existingVariant == null) {
            ModelVariant variant = new ModelVariant();
            variant.setName(name);
            variant.setModel(model);
            variantRepository.save(variant);
            return variant.getId();
        } else {
           return existingVariant.getId();
        }
    }

    @Override
    public void deleteVariant(long variantId) {
        variantRepository.deleteById(variantId);
    }
}
