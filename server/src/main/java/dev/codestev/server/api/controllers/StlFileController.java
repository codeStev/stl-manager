package dev.codestev.server.api.controllers;

import dev.codestev.server.api.dto.StlFileDetailsDto;
import dev.codestev.server.api.mapping.StlFileDtoMapper;
import dev.codestev.server.business.model.StlFileDetails;
import dev.codestev.server.business.service.FileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/file")
public class StlFileController {

    private final FileService fileService;
    private final StlFileDtoMapper stlFileDtoMapper;

    public StlFileController(FileService fileService, StlFileDtoMapper stlFileDtoMapper) {
        this.fileService = fileService;
        this.stlFileDtoMapper = stlFileDtoMapper;
    }

    @GetMapping(value = "/{id}")
    public StlFileDetailsDto getStlFileDetails(@PathVariable long id){
        StlFileDetails details = fileService.getStlFileDetails(id);
        return stlFileDtoMapper.toDetailsDto(details);
    }



}
