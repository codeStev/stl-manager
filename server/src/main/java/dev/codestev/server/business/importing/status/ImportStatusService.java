package dev.codestev.server.business.importing.status;

import dev.codestev.server.api.dto.importing.ImportStatusDto;

// Java
public interface ImportStatusService {
    boolean tryStart();                 // returns false if already running
    void markSuccess();
    void markFailure(Throwable t);
    void setTotalLibraries(long total);          // optional: set once when the run starts
    void setTotalModels(long total);          // optional: set once when the run starts
    void incrementProcessedLibraries();          // optional: call as each unit completes
    void incrementProcessedModels();          // optional: call as each unit completes
    ImportStatusDto snapshot();
}