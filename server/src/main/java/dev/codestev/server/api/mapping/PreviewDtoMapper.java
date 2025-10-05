package dev.codestev.server.api.mapping;

import dev.codestev.server.api.dto.model.ModelPreviewRefDto;
import dev.codestev.server.business.mapping.MapstructCentralConfig;
import dev.codestev.server.business.model.ModelPreview;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapstructCentralConfig.class)
public interface PreviewDtoMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id",       source = "id")
    @Mapping(target = "path",     source = "path")
    @Mapping(target = "width",    source = "width")
    @Mapping(target = "height",   source = "height")
    @Mapping(target = "mime",     source = "mime")
    @Mapping(target = "position", source = "position")
    ModelPreviewRefDto toDto(ModelPreview ref);
}
