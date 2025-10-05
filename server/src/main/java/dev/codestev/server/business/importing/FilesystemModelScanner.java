package dev.codestev.server.business.importing;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.*;

public class FilesystemModelScanner {

    public record ScannedModel(String artist, String model, String name, List<ScannedStlFile> files) {}
    public record ScannedStlFile(String fileName, Optional<String> variant, String relativePath, long size, long lastModifiedMillis, Optional<String> sha256) {}

    public Map<String, ScannedModel> scan(Path root, boolean computeHashes) throws IOException {
        if (root == null) throw new IllegalArgumentException("Root path is null");
        if (!Files.isDirectory(root)) throw new NoSuchFileException("Models root not found: " + root);

        Map<String, ScannedModel> result = new LinkedHashMap<>();

        // First level: artist directories
        try (DirectoryStream<Path> artists = Files.newDirectoryStream(root)) {
            for (Path artistDir : artists) {
                if (!Files.isDirectory(artistDir)) continue;
                String artistName = artistDir.getFileName().toString();

                // Second level: model directories under each artist
                try (DirectoryStream<Path> models = Files.newDirectoryStream(artistDir)) {
                    for (Path modelDir : models) {
                        if (!Files.isDirectory(modelDir)) continue;

                        String modelName = modelDir.getFileName().toString();
                        String modelKey = artistName + "/" + modelName; // unique across artists

                        List<ScannedStlFile> files = new ArrayList<>();

                        // Walk entire model subtree (base files + variants)
                        Files.walkFileTree(modelDir, new SimpleFileVisitor<>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                if (isStlFile(file)) {
                                    String rel = root.relativize(file).toString().replace('\\', '/');
                                    long size = attrs.size();
                                    long mtime = attrs.lastModifiedTime().toMillis();
                                    Optional<String> hash = computeHashes ? Optional.of(sha256(file)) : Optional.empty();
                                    String fileNameNoExt = stripStlExtension(file.getFileName().toString());

                                    // Determine variant = first subdirectory under the model dir (if any)
                                    Path relToModel = modelDir.relativize(file);
                                    Optional<String> variant = relToModel.getNameCount() > 1
                                            ? Optional.of(relToModel.getName(0).toString())
                                            : Optional.empty();

                                    files.add(new ScannedStlFile(fileNameNoExt, variant, rel, size, mtime, hash));
                                }
                                return FileVisitResult.CONTINUE;
                            }
                        });

                        // Stable ordering: by variant, then relative path
                        files.sort(
                                Comparator.comparing((ScannedStlFile f) -> f.variant().orElse(""))
                                        .thenComparing(ScannedStlFile::relativePath)
                        );

                        result.put(modelKey, new ScannedModel(artistName, modelName, modelKey, files));
                    }
                }
            }
        }

        return result;
    }

    private static boolean isStlFile(Path file) {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        if (dot < 0) return false;
        String ext = name.substring(dot + 1);
        return ext.equalsIgnoreCase("stl");
    }

    private static String stripStlExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot > 0 && fileName.substring(dot + 1).equalsIgnoreCase("stl")) {
            return fileName.substring(0, dot);
        }
        return fileName;
    }

    private static String sha256(Path file) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (var in = Files.newInputStream(file); var dis = new DigestInputStream(in, md)) {
                dis.transferTo(OutputStream.nullOutputStream());
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IOException("Failed to compute SHA-256 for " + file, e);
        }
    }
}
