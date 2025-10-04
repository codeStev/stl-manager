package dev.codestev.server.api.mapping;

import dev.codestev.server.api.dto.model.ModelVariantDetailedDto;
import dev.codestev.server.api.dto.model.ModelVariantRefDto;
import dev.codestev.server.api.dto.stl.StlFileRefDto;
import dev.codestev.server.business.mapping.MapstructCentralConfig;
import dev.codestev.server.business.model.ModelVariantDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(
    config = MapstructCentralConfig.class,
    uses = { StlFileDtoMapper.class }
)
public interface ModelVariantDtoMapper {

    @Mapping(target = "id",   source = "id")
    @Mapping(target = "name", source = "name")
    ModelVariantRefDto toRefDto(ModelVariantDetails ref);

    @Mapping(target = "id",             source = "id")
    @Mapping(target = "name",           source = "name")
    @Mapping(target = "inheritBaseFiles", source = "inheritBaseFiles")
    @Mapping(target = "stlFiles",       source = "stlFiles")
    @Mapping(target = "effectiveFiles", ignore = true)
    ModelVariantDetailedDto toDetailedDto(ModelVariantDetails details);

    List<ModelVariantRefDto> toRefDtoList(List<ModelVariantDetails> refs);
    List<StlFileRefDto> toStlFileRefDtoList(Set<dev.codestev.server.business.model.StlFileRef> refs);
}
