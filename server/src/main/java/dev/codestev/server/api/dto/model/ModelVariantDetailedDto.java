package dev.codestev.server.api.dto.model;

import dev.codestev.server.api.dto.stl.StlFileRefDto;

import java.util.List;

public record ModelVariantDetailedDto(
    Long id,
    String name,
    boolean inheritBaseFiles,
    List<StlFileRefDto> effectiveFiles
) {}
