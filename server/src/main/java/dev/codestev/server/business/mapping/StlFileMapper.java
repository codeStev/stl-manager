package dev.codestev.server.business.mapping;

import dev.codestev.server.business.model.StlFileRef;
import dev.codestev.server.persistence.model.StlFile;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(config = MapstructCentralConfig.class)
public interface StlFileMapper {
    StlFileRef toRef(StlFile entity);
    List<StlFileRef> toRefList(Set<StlFile> entities);
}

