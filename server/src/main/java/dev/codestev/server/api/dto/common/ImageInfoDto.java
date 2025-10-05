package dev.codestev.server.api.dto.common;

public record ImageInfoDto(
    String path,     // e.g. the thumbnail path or URL
    Integer width,
    Integer height,
    String mime
) {}
