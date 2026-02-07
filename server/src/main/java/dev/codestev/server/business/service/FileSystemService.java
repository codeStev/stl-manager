package dev.codestev.server.business.service;

import dev.codestev.server.api.dto.DirectoryDto;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class FileSystemService {

    public List<DirectoryDto> listDirectories(String pathStr) {
        Path path = pathStr != null ? Paths.get(pathStr) : getDefaultStartPath();
        List<DirectoryDto> directories = new ArrayList<>();

        try {
            // Add parent directory entry if not at root
            if (path.getParent() != null) {
                Path parent = path.getParent();
                directories.add(new DirectoryDto(
                        "..",
                        parent.toAbsolutePath().toString(),
                        parent.getParent() != null ? parent.getParent().toString() : null,
                        true,
                        Files.isReadable(parent),
                        Files.isWritable(parent),
                        0,
                        Files.getLastModifiedTime(parent).toMillis()
                ));
            }

            // List directories in current path
            try (Stream<Path> paths = Files.list(path)) {
                paths.filter(Files::isDirectory)
                     .filter(Files::isReadable)
                     .sorted()
                     .forEach(dir -> {
                         try {
                             BasicFileAttributes attrs = Files.readAttributes(dir, BasicFileAttributes.class);
                             directories.add(new DirectoryDto(
                                     dir.getFileName().toString(),
                                     dir.toAbsolutePath().toString(),
                                     path.toString(),
                                     true,
                                     Files.isReadable(dir),
                                     Files.isWritable(dir),
                                     attrs.size(),
                                     attrs.lastModifiedTime().toMillis()
                             ));
                         } catch (IOException e) {
                             // Skip directories we can't read
                         }
                     });
            }
        } catch (IOException e) {
            // Return empty list if path is invalid
        }

        return directories;
    }

    public boolean isValidDirectory(String pathStr) {
        try {
            Path path = Paths.get(pathStr);
            return Files.exists(path) && Files.isDirectory(path) && Files.isReadable(path);
        } catch (Exception e) {
            return false;
        }
    }

    private Path getDefaultStartPath() {
        // Start from user home directory or root based on OS
        String userHome = System.getProperty("user.home");
        if (userHome != null && Files.exists(Paths.get(userHome))) {
            return Paths.get(userHome);
        }
        return Paths.get("/"); // Unix root or adjust for Windows
    }
}