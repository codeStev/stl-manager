package dev.codestev.server.api.dto;

public record DirectoryDto(
        String name,
        String absolutePath,
        String parentPath,
        boolean isDirectory,
        boolean canRead,
        boolean canWrite,
        long size,
        long lastModified
) {}