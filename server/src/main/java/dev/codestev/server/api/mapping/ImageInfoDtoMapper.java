package dev.codestev.server.api.mapping;

import dev.codestev.server.api.dto.common.ImageInfoDto;
import dev.codestev.server.business.mapping.MapstructCentralConfig;
import dev.codestev.server.business.model.ImageInfo; // business VO
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapstructCentralConfig.class)
public interface ImageInfoDtoMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "path",   source = "path")
    @Mapping(target = "width",  source = "width")
    @Mapping(target = "height", source = "height")
    @Mapping(target = "mime",   source = "mime")
    ImageInfoDto toDto(ImageInfo info);
}
