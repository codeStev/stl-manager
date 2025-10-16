package dev.codestev.server.api.controllers;

import dev.codestev.server.api.dto.importing.ImportStatusDto;
import dev.codestev.server.business.importing.ModelImportService;
import dev.codestev.server.business.importing.status.ImportStatusService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/import")
public class ImportController {

    private final ModelImportService modelImportService;
    private final ImportStatusService importStatusService;
    private final TaskExecutor taskExecutor;

    public ImportController(
            ModelImportService modelImportService,
            ImportStatusService importStatusService,
            @Qualifier("ImportTaskExecutor") TaskExecutor taskExecutor
    ) {
        this.modelImportService = modelImportService;
        this.importStatusService = importStatusService;
        this.taskExecutor = taskExecutor;
    }

    @PostMapping("/")
    public ResponseEntity<Void> startFullSync() {
        // Prevent concurrent runs
        if (!importStatusService.tryStart()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        taskExecutor.execute(() -> {
            try {
                modelImportService.runFullSync();
                importStatusService.markSuccess();
            } catch (Exception ex) {
                importStatusService.markFailure(ex);
            }
        });

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping("/status")
    public ResponseEntity<ImportStatusDto> status() {
        return ResponseEntity.ok(importStatusService.snapshot());
    }
}