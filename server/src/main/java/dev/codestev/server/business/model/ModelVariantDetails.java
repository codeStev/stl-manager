package dev.codestev.server.business.model;

import java.util.Set;

public record ModelVariantDetails(
        Long id,
        String name,
        Set<StlFileRef> stlFiles,
        boolean inheritBaseFiles
) {}

