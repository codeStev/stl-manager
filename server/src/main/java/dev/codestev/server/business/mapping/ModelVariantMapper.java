package dev.codestev.server.business.mapping;

import dev.codestev.server.business.model.ModelVariantRef;
import dev.codestev.server.persistence.model.ModelVariant;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = MapstructCentralConfig.class)
public interface ModelVariantMapper {
    ModelVariantRef toRef(ModelVariant entity);
    List<ModelVariantRef> toRefList(List<ModelVariant> entities);
}

