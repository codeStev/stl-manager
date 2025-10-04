package dev.codestev.server.business.model;

import java.util.List;

public record ModelVariantDetails(
        Long id,
        String name,
        List<StlFileRef> stlFiles,
        boolean inheritBaseFiles
) {}

