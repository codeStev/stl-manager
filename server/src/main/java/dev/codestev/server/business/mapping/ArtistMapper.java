package dev.codestev.server.business.mapping;

import dev.codestev.server.business.model.ArtistRef;
import dev.codestev.server.business.model.ImageInfo;
import dev.codestev.server.persistence.model.Artist;
import dev.codestev.server.persistence.model.Model;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapstructCentralConfig.class)
public interface ArtistMapper {

    @Mapping(target = "modelIds", expression = "java(toModelIds(entity.getModels()))")
    ArtistRef toRef(Artist entity);


    default List<Long> toModelIds(List<Model> entity) {
        return entity.stream().map(Model::getId).toList();
    }
}

