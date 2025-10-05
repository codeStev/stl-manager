package dev.codestev.server.business.model;

import java.util.List;

public record ArtistRef(
        String id,
        String name,
        String homepage,
        List<Long> modelIds
) {}

