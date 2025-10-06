package dev.codestev.server.business.importing;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.time.Duration;

@ConfigurationProperties(prefix = "importer.models")
public class ModelImportProperties {
   //TODO remove root
    private Path root;

    /**
     * Enable/disable startup import.
     */
    private boolean enabled = true;

    /**
     * If true, models present in DB but missing on disk will be deleted.
     * If false, they’re left untouched.
     */
    private boolean deleteOrphans = true;

    /**
     * Optional scheduled scanning (in addition to startup). Disabled by default.
     */
    private boolean scheduleEnabled = false;

    /**
     * Interval for scheduled scans.
     */
    private Duration scheduleInterval = Duration.ofMinutes(30);

    /**
     * Whether to compute file hashes (expensive for large STL files). If false, size+mtime is used.
     */
    private boolean computeHashes = false;

    // getters and setters
    public Path getRoot() { return root; }
    public void setRoot(Path root) { this.root = root; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isDeleteOrphans() { return deleteOrphans; }
    public void setDeleteOrphans(boolean deleteOrphans) { this.deleteOrphans = deleteOrphans; }
    public boolean isScheduleEnabled() { return scheduleEnabled; }
    public void setScheduleEnabled(boolean scheduleEnabled) { this.scheduleEnabled = scheduleEnabled; }
    public Duration getScheduleInterval() { return scheduleInterval; }
    public void setScheduleInterval(Duration scheduleInterval) { this.scheduleInterval = scheduleInterval; }
    public boolean isComputeHashes() { return computeHashes; }
    public void setComputeHashes(boolean computeHashes) { this.computeHashes = computeHashes; }
}
