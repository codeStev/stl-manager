package dev.codestev.server.api.mapping;

import dev.codestev.server.api.dto.model.ModelDetailedDto;
import dev.codestev.server.api.dto.model.ModelPreviewRefDto;
import dev.codestev.server.api.dto.stl.StlFileRefDto;
import dev.codestev.server.business.mapping.MapstructCentralConfig;
import dev.codestev.server.business.model.ModelDetailed;
import dev.codestev.server.business.model.ModelPreview;
import dev.codestev.server.business.model.StlFileRef;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(
    config = MapstructCentralConfig.class,
    uses = {
        LibraryDtoMapper.class,     // from your existing API mapper (business LibraryRef -> LibraryDto)
        ArtistDtoMapper.class,         // from your existing API mapper (business ArtistRef -> ArtistSummaryDto)
        ImageInfoDtoMapper.class,   // business ImageInfo -> ImageInfoDto
        PreviewDtoMapper.class,     // business ModelPreviewRef -> ModelPreviewDto
        StlFileDtoMapper.class,     // business StlFileRef -> StlFileRefDto
        ModelVariantDtoMapper.class // business ModelVariantRef/Details -> DTOs
    }
)
public interface ModelDtoMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id",        source = "id")
    @Mapping(target = "name",      source = "name")
    @Mapping(target = "library",   source = "library")    // LibraryRef -> LibraryDto
    @Mapping(target = "artist",    source = "artist")     // ArtistRef -> ArtistSummaryDto
    @Mapping(target = "thumbnail", source = "thumbnail")  // ImageInfo -> ImageInfoDto
    @Mapping(target = "previews",  source = "previews")   // List<ModelPreviewRef> -> List<ModelPreviewDto>
    @Mapping(target = "stlFiles",  source = "stlFiles")   // Set<StlFileRef> -> List<StlFileRefDto> (order as defined by business)
    @Mapping(target = "variants",  source = "variants")   // List<ModelVariantRef> -> List<ModelVariantRefDto>
    ModelDetailedDto toDetailedDto(ModelDetailed details);

    List<ModelPreviewRefDto> toPreviewDtoList(List<ModelPreview> refs);
    List<StlFileRefDto> toStlFileRefDtoList(Set<StlFileRef> refs);
}
