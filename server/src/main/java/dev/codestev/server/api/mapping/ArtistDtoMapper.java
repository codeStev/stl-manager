package dev.codestev.server.api.mapping;

import dev.codestev.server.api.dto.common.ArtistSummaryDto;
import dev.codestev.server.business.mapping.MapstructCentralConfig;
import dev.codestev.server.business.model.ArtistRef;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapstructCentralConfig.class)
public interface ArtistDtoMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    ArtistSummaryDto toDto(ArtistRef ref);

}
