package dev.codestev.server.api.dto.model;

import dev.codestev.server.api.dto.stl.StlFileRefDto;

import java.util.List;

public record ModelVariantRefDto(
    Long id,
    String name,
    List<StlFileRefDto> stlFiles
) {}
