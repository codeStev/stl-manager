package dev.codestev.server.business.service;

import dev.codestev.server.business.mapping.LibraryRefMapper;
import dev.codestev.server.business.model.LibraryRef;
import dev.codestev.server.persistence.LibraryRepository;
import dev.codestev.server.persistence.model.Library;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

@Service
public class LibraryService {

    private final LibraryRepository libraryRepository;
    private final LibraryRefMapper refMapper;

    public LibraryService(LibraryRepository libraryRepository, LibraryRefMapper refMapper) {
        this.libraryRepository = libraryRepository;
        this.refMapper = refMapper;
    }

    @Transactional
    public LibraryRef createLibrary(String name, String pathStr) {
        String normalizedPath = normalizeAndValidatePath(pathStr);

        String finalName = (name == null || name.isBlank())
                ? deriveNameFromPath(normalizedPath)
                : name.trim();

        if (libraryRepository.existsByNameIgnoreCase(finalName)) {
            throw new IllegalArgumentException("A library with that name already exists: " + finalName);
        }
        if (libraryRepository.existsByPathIgnoreCase(pathStr)) {
            throw new IllegalArgumentException("A library with that path already exists: " + pathStr);
        }

        var existingByPath = libraryRepository.findByPath(normalizedPath);
        if (existingByPath.isPresent()) {
            return refMapper.toRef(existingByPath.get());
        }

        var entity = new Library();
        entity.setName(finalName);
        entity.setPath(normalizedPath);

        try {
            var saved = libraryRepository.save(entity);
            return refMapper.toRef(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Could not create library. Name must be unique: " + finalName, ex);
        }
    }

    @Transactional(readOnly = true)
    public LibraryRef getLibrary(long id) {
        var entity = libraryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Library not found: " + id));
        return refMapper.toRef(entity);
    }

    @Transactional(readOnly = true)
    public List<LibraryRef> listLibraries() {
        var entities = libraryRepository.findAll().stream()
                .sorted(Comparator.comparing(Library::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        return refMapper.toRefs(entities);
    }

    private String normalizeAndValidatePath(String pathStr) {
        if (pathStr == null || pathStr.isBlank()) {
            throw new IllegalArgumentException("Path is required.");
        }
        Path path = Paths.get(pathStr).toAbsolutePath().normalize();
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Path does not exist: " + path);
        }
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path is not a directory: " + path);
        }
        if (!Files.isReadable(path)) {
            throw new IllegalArgumentException("Path is not readable: " + path);
        }
        try {
            return path.toRealPath().toString();
        } catch (Exception ignored) {
            return path.toString();
        }
    }

    private String deriveNameFromPath(String normalizedPath) {
        Path p = Paths.get(normalizedPath);
        var fileName = p.getFileName();
        if (fileName == null) {
            throw new IllegalArgumentException("Cannot derive library name from path: " + normalizedPath);
        }
        return fileName.toString();
    }
}


