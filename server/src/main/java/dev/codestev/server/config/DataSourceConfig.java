package dev.codestev.server.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Locale;
import java.util.Optional;

@Configuration
public class DataSourceConfig {

    @Bean
    ApplicationRunner logDatabaseLocation(DataSource dataSource, Environment env) {
        return args -> {
            Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

            String jdbcUrl = resolveJdbcUrl(dataSource, env);
            String redacted = redactSensitive(jdbcUrl);
            log.info("Database JDBC URL: {}", redacted);

            String filePath = resolveDatabaseFilePath(jdbcUrl);
            if (filePath != null) {
                Path absolute = Paths.get(filePath).toAbsolutePath().normalize();
                log.info("Database file path: {}", absolute);
            } else if (isInMemory(jdbcUrl)) {
                log.info("Database is in-memory (no file on disk).");
            }
        };
    }

    // helpers to add in the same class
    private String resolveJdbcUrl(DataSource dataSource, Environment env) {
        String configured = env.getProperty("spring.datasource.url");
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
        try (Connection conn = dataSource.getConnection()) {
            return conn.getMetaData().getURL();
        } catch (Exception e) {
            LoggerFactory.getLogger(DataSourceConfig.class)
                    .warn("Could not determine JDBC URL from DataSource: {}", e.getMessage());
            return "<unknown>";
        }
    }

    private String resolveDatabaseFilePath(String jdbcUrl) {
        if (jdbcUrl == null) return null;

        // SQLite
        if (jdbcUrl.startsWith("jdbc:sqlite:")) {
            String path = jdbcUrl.substring("jdbc:sqlite:".length());
            if (":memory:".equals(path)) return null;
            return path;
        }

        // H2 (file mode)
        if (jdbcUrl.startsWith("jdbc:h2:file:")) {
            String path = jdbcUrl.substring("jdbc:h2:file:".length());
            int paramsIdx = path.indexOf(';');
            if (paramsIdx >= 0) path = path.substring(0, paramsIdx);
            return path + ".mv.db";
        }

        return null;
    }

    private boolean isInMemory(String jdbcUrl) {
        if (jdbcUrl == null) return false;
        return jdbcUrl.startsWith("jdbc:h2:mem:")
                || jdbcUrl.equals("jdbc:sqlite::memory:");
    }

    private String redactSensitive(String url) {
        if (url == null) return null;
        String redacted = url
                .replaceAll("(?i)(password=)[^;&]+", "$1****")
                .replaceAll("(?i)(;password=)[^;]+", "$1****");
        return redacted.replaceAll("(?i)(://[^/:]+:)[^@/]+@", "$1****@");
    }


    @Bean
    public DataSource dataSource() throws Exception {
        String appName = "stl-manager";
        Path appDataDir = resolveAppDataDir(appName);
        Files.createDirectories(appDataDir);

        Path dbPath = appDataDir.resolve("app.db");

        String jdbcUrl = "jdbc:sqlite:" + dbPath.toString();

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(jdbcUrl);
        cfg.setDriverClassName("org.sqlite.JDBC");
        cfg.setConnectionInitSql("PRAGMA foreign_keys=ON;PRAGMA journal_mode=WAL;PRAGMA busy_timeout=5000;");
        cfg.setMaximumPoolSize(5);
        cfg.setPoolName("sqlite");

        return new HikariDataSource(cfg);
    }

    private static Path resolveAppDataDir(String appName) {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);

        if (os.contains("win")) {
            String base = Optional.ofNullable(System.getenv("LOCALAPPDATA"))
                    .orElseGet(() -> System.getProperty("user.home"));
            return Paths.get(base, appName);
        }

        if (os.contains("mac") || os.contains("darwin")) {
            String home = System.getProperty("user.home");
            return Paths.get(home, "Library", "Application Support", appName);
        }

        String base = Optional.ofNullable(System.getenv("XDG_DATA_HOME"))
                .orElseGet(() -> Paths.get(System.getProperty("user.home"), ".local", "share").toString());
        return Paths.get(base, appName);
    }
}
