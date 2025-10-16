package dev.codestev.server.api.dto.importing;// Java
import dev.codestev.server.business.importing.status.ImportState;

import java.time.Instant;

public record ImportStatusDto(
        ImportState state,
        Instant startedAt,
        Instant finishedAt,
        long totalLibraries,
        long totalModels,
        long processedLibraries,
        long processedModels,
        String lastError
) {}