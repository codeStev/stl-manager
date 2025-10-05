package dev.codestev.server.business.mapping;

import dev.codestev.server.business.model.ImageInfo;
import dev.codestev.server.business.model.LibraryRef;
import dev.codestev.server.business.model.ModelDetailed;
import dev.codestev.server.persistence.model.Model;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(
        config = MapstructCentralConfig.class,
        uses = {
                ModelPreviewMapper.class,
                ModelVariantMapper.class,
                StlFileMapper.class,
                LibraryRefMapper.class,
        }
)
public interface ModelMapper {

    @BeanMapping(ignoreByDefault = false)
    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "artist.id", source = "artist.id"),
            @Mapping(target = "thumbnail", expression = "java(toImageInfo(entity))"),
            @Mapping(target = "previews", source = "previews"),
            @Mapping(target = "stlFiles", source = "stlFiles"),
            @Mapping(target = "variants", source = "variants")
    })
    ModelDetailed toBusiness(Model entity);

    List<ModelDetailed> toBusinessList(List<Model> entities);

    default ImageInfo toImageInfo(Model entity) {
        if (entity == null) return null;
        var path = entity.getThumbnailPath();
        var width = entity.getThumbnailWidth();
        var height = entity.getThumbnailHeight();
        var mime = entity.getThumbnailMime();
        if (path == null && width == null && height == null && mime == null) {
            return null;
        }
        return new ImageInfo(path, width, height, mime);
    }

}

