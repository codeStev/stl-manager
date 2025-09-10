package dev.codestev.server.business.model;
import java.util.Comparator;


public record ModelPreview(
        Long id,
        String path,
        Integer width,
        Integer height,
        String mime,
        Integer position
) {
    public ModelPreview {
        if (path != null && path.isBlank()) path = null;
        if (mime != null && mime.isBlank()) mime = null;

        if (width != null && width <= 0) throw new IllegalArgumentException("width must be positive");
        if (height != null && height <= 0) throw new IllegalArgumentException("height must be positive");
    }

    public static final Comparator<ModelPreview> ORDERING =
            Comparator.comparing(ModelPreview::position, Comparator.nullsLast(Integer::compareTo))
                    .thenComparing(ModelPreview::id, Comparator.nullsLast(Long::compareTo));

}

