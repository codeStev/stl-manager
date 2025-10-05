package dev.codestev.server.business.mapping;

import dev.codestev.server.business.model.ModelPreview;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = MapstructCentralConfig.class)
public interface ModelPreviewMapper {
    ModelPreview toBusiness(dev.codestev.server.persistence.model.ModelPreview entity);
    List<ModelPreview> toBusinessList(List<dev.codestev.server.persistence.model.ModelPreview> entities);
}

