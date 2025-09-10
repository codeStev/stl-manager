package dev.codestev.server.api.mapping;

import dev.codestev.server.api.dto.LibraryDto;
import dev.codestev.server.business.model.LibraryRef;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LibraryDtoMapper {
    LibraryDto toDto(LibraryRef ref);
    List<LibraryDto> toDtoList(List<LibraryRef> refs);
}


