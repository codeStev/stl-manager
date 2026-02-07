package dev.codestev.server.api.controllers;

import dev.codestev.server.api.dto.DirectoryDto;
import dev.codestev.server.business.service.FileSystemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/filesystem")
public class FileSystemController {

    private final FileSystemService fileSystemService;

    public FileSystemController(FileSystemService fileSystemService) {
        this.fileSystemService = fileSystemService;
    }

    @GetMapping("/directories")
    public ResponseEntity<List<DirectoryDto>> listDirectories(
            @RequestParam(required = false) String path) {
        List<DirectoryDto> directories = fileSystemService.listDirectories(path);
        return ResponseEntity.ok(directories);
    }

    @GetMapping("/validate-path")
    public ResponseEntity<Boolean> validatePath(@RequestParam String path) {
        boolean isValid = fileSystemService.isValidDirectory(path);
        return ResponseEntity.ok(isValid);
    }
}