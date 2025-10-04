package dev.codestev.server.api.mapping;

import dev.codestev.server.api.dto.StlFileDetailsDto;
import dev.codestev.server.api.dto.stl.StlFileRefDto;
import dev.codestev.server.business.mapping.MapstructCentralConfig;
import dev.codestev.server.business.model.StlFileDetails;
import dev.codestev.server.business.model.StlFileRef;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapstructCentralConfig.class)
public interface StlFileDtoMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id",       source = "id")
    @Mapping(target = "fileName", source = "fileName")
    StlFileRefDto toRefDto(StlFileRef ref);

    StlFileDetailsDto toDetailsDto(StlFileDetails details);
}
