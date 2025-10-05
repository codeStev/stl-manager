package dev.codestev.server.api.controllers;

import dev.codestev.server.api.dto.StlFileDetailsDto;
import dev.codestev.server.api.mapping.StlFileDtoMapper;
import dev.codestev.server.business.model.FileContent;
import dev.codestev.server.business.model.StlFileDetails;
import dev.codestev.server.business.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

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

//TODO Fix all the fcked up api paths
    @GetMapping(
            value = "/{id}/content",
            produces = { "model/stl", "application/sla", MediaType.APPLICATION_OCTET_STREAM_VALUE }
    )
    public ResponseEntity<Resource> getStlContent(@PathVariable long id) throws IOException {
        FileContent fileContent = fileService.getFileContent(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.inline().filename(fileContent.getFileName()).build());
        headers.setCacheControl(CacheControl.noCache().getHeaderValue());

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(fileContent.getContentType()))
                .contentLength(fileContent.getFileLength())
                .body(fileContent.getContent());
    }

}
