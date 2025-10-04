package dev.codestev.server.api.dto.model;

public record ModelPreviewRefDto(
    Long id,
    String path,     // path or URL to the preview image
    Integer width,
    Integer height,
    String mime,
    Integer position
) {}
