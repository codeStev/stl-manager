package dev.codestev.server.api;

import dev.codestev.server.api.dto.CreateLibraryRequest;
import dev.codestev.server.api.dto.LibraryDto;
import dev.codestev.server.api.mapping.LibraryDtoMapper;
import dev.codestev.server.business.service.LibraryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/libraries")
public class LibraryController {

    private final LibraryService libraryService;
    private final LibraryDtoMapper libraryDtoMapper;

    public LibraryController(LibraryService libraryService, LibraryDtoMapper libraryDtoMapper) {
        this.libraryService = libraryService;
        this.libraryDtoMapper = libraryDtoMapper;
    }

    @PostMapping
    public ResponseEntity<LibraryDto> create(@RequestBody CreateLibraryRequest body) {
        var ref = libraryService.createLibrary(body.name(), body.path());
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryDtoMapper.toDto(ref));
    }

    @GetMapping("/{id}")
    public LibraryDto get(@PathVariable long id) {
        var ref = libraryService.getLibrary(id);
        return libraryDtoMapper.toDto(ref);
    }

    @GetMapping
    public List<LibraryDto> list() {
        var refs = libraryService.listLibraries();
        return libraryDtoMapper.toDtoList(refs);
    }
}
