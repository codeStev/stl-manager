package dev.codestev.server.api.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LibraryDto(
        @JsonProperty("library_id")
        Long id,
        @JsonProperty("library_name")
        String name,
        @JsonProperty("library_path")
        String path
) {}

