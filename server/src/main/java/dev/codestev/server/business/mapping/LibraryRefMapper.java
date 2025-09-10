package dev.codestev.server.business.mapping;

import dev.codestev.server.business.model.LibraryRef;
import dev.codestev.server.persistence.model.Library;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = MapstructCentralConfig.class)
public interface LibraryRefMapper {
    LibraryRef toRef(Library entity);
    List<LibraryRef> toRefs(List<Library> entities);
}

