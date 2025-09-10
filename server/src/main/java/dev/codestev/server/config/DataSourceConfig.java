package dev.codestev.server.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;

@Configuration
public class DataSourceConfig {
    @Bean
    public DataSource dataSource() throws Exception {
        String appName = "stl-manager"; // TODO: change to your app name
        Path appDataDir = resolveAppDataDir(appName);
        Files.createDirectories(appDataDir);

        Path dbPath = appDataDir.resolve("app.db");

        String jdbcUrl = "jdbc:sqlite:" + dbPath.toString();

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(jdbcUrl);
        cfg.setDriverClassName("org.sqlite.JDBC");
        cfg.setConnectionInitSql("PRAGMA foreign_keys=ON;");
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
