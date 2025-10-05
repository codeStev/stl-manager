package dev.codestev.server.business.model;

public record ImageInfo(
        String path,
        Integer width,
        Integer height,
        String mime
) {
    public ImageInfo {
        if (path != null && path.isBlank()) path = null;
        if (mime != null && mime.isBlank()) mime = null;

        if (width != null && width <= 0) throw new IllegalArgumentException("width must be positive");
        if (height != null && height <= 0) throw new IllegalArgumentException("height must be positive");

    }
}

