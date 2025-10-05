package dev.codestev.server.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateLibraryRequest(
        @JsonProperty("library_name")
        String name,
        @JsonProperty("library_path")
        String path
) {}