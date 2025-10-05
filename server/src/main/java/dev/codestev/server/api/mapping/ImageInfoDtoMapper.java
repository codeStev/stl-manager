package dev.codestev.server.api.mapping;

import dev.codestev.server.api.dto.common.ImageInfoDto;
import dev.codestev.server.business.mapping.MapstructCentralConfig;
import dev.codestev.server.business.model.ImageInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapstructCentralConfig.class)
public interface ImageInfoDtoMapper {

    @Mapping(target = "path",   source = "path")
    @Mapping(target = "width",  source = "width")
    @Mapping(target = "height", source = "height")
    @Mapping(target = "mime",   source = "mime")
    ImageInfoDto toDto(ImageInfo info);
}
