package dev.codestev.server.business.importing.status;// Java
import dev.codestev.server.api.dto.importing.ImportStatusDto;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class InMemoryImportStatusService implements ImportStatusService {

    private final ReentrantLock lock = new ReentrantLock();

    private volatile ImportState state = ImportState.IDLE;
    private volatile Instant startedAt;
    private volatile Instant finishedAt;
    private final AtomicLong totalLibraries = new AtomicLong();
    private final AtomicLong totalModels = new AtomicLong();
    private final AtomicLong processedLibraries = new AtomicLong();
    private final AtomicLong processedModels = new AtomicLong();
    private volatile String lastError;

    @Override
    public boolean tryStart() {
        lock.lock();
        try {
            if (state == ImportState.RUNNING) return false;
            state = ImportState.RUNNING;
            startedAt = Instant.now();
            finishedAt = null;
            lastError = null;
            totalLibraries.set(0);
            totalLibraries.set(0);
            processedLibraries.set(0);
            processedModels.set(0);
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void markSuccess() {
        lock.lock();
        try {
            state = ImportState.SUCCESS;
            finishedAt = Instant.now();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void markFailure(Throwable t) {
        lock.lock();
        try {
            state = ImportState.FAILED;
            finishedAt = Instant.now();
            lastError = t != null ? t.getMessage() : "Unknown error";
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setTotalLibraries(long totalLibraries) {

        this.totalLibraries.set(Math.max(0, totalLibraries));
    }

    @Override
    public void setTotalModels(long totalModels) {
        this.totalModels.set(Math.max(0, totalModels));
    }

    @Override
    public void incrementProcessedLibraries() {
        processedLibraries.incrementAndGet();
    }

    @Override
    public void incrementProcessedModels() {
        processedModels.incrementAndGet();
    }

    @Override
    public ImportStatusDto snapshot() {
        return new ImportStatusDto(
                state,
                startedAt,
                finishedAt,
                totalLibraries.get(),
                totalModels.get(),
                processedLibraries.get(),
                processedModels.get(),
                lastError
        );
    }
}