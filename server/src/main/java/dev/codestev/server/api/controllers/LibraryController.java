package dev.codestev.server.api.controllers;

import dev.codestev.server.api.dto.CreateLibraryRequest;
import dev.codestev.server.api.dto.common.LibraryDto;
import dev.codestev.server.api.dto.model.ModelDetailedDto;
import dev.codestev.server.api.mapping.LibraryDtoMapper;
import dev.codestev.server.api.mapping.ModelDtoMapper;
import dev.codestev.server.business.model.ModelDetailed;
import dev.codestev.server.business.service.LibraryService;
import dev.codestev.server.business.service.ModelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/libraries")
public class LibraryController {

    private final LibraryService libraryService;
    private final LibraryDtoMapper libraryDtoMapper;
    private final ModelService modelService;
    private final ModelDtoMapper modelDtoMapper;

    public LibraryController(LibraryService libraryService, LibraryDtoMapper libraryDtoMapper, ModelService modelService, ModelDtoMapper modelDtoMapper) {
        this.libraryService = libraryService;
        this.libraryDtoMapper = libraryDtoMapper;
        this.modelService = modelService;
        this.modelDtoMapper = modelDtoMapper;
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

    @GetMapping("/{id}/model")
    public List<ModelDetailedDto> list(@PathVariable long id) {
        List<ModelDetailed> allModelDetailsByLibraryId = modelService.getAllModelDetailsByLibraryId(id);
        return allModelDetailsByLibraryId.stream().map(modelDtoMapper::toDetailedDto).toList();
    }
}
