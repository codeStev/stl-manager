package dev.codestev.server.business.mapping;

import dev.codestev.server.business.model.ModelVariantDetails;
import dev.codestev.server.persistence.model.ModelVariant;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = MapstructCentralConfig.class)
public interface ModelVariantMapper {
    ModelVariantDetails toRef(ModelVariant entity);
    List<ModelVariantDetails> toRefList(List<ModelVariant> entities);
}

