package dev.codestev.server.api.dto.model;

import dev.codestev.server.api.dto.common.ArtistSummaryDto;
import dev.codestev.server.api.dto.common.ImageInfoDto;
import dev.codestev.server.api.dto.common.LibraryDto;
import dev.codestev.server.api.dto.stl.StlFileRefDto;

import java.util.List;

public record ModelDetailedDto(
        Long id,
        String name,
        LibraryDto library,
        ArtistSummaryDto artist,     // nullable if a model may not have an artist
        ImageInfoDto thumbnail,      // nullable if thumbnail not set
        List<ModelPreviewRefDto> previews,
        List<StlFileRefDto> stlFiles,
        List<ModelVariantRefDto> variants
) {}
