package dev.codestev.server.business.model;

import java.util.List;

public record ModelDetailed(
        Long id,
        String name,
        LibraryRef library,
        ArtistSummary artist,
        ImageInfo thumbnail,
        List<ModelPreview> previews,
        List<StlFileRef> stlFiles,
        List<ModelVariantDetails> variants
) {}

