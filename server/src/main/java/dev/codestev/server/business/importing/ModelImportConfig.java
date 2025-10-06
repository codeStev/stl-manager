package dev.codestev.server.business.importing;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(ModelImportProperties.class)
public class ModelImportConfig {

    @Bean
    FilesystemModelScanner filesystemModelScanner() {
        return new FilesystemModelScanner();
    }

    @Bean
    ModelImportService modelImportService(
            ModelImportProperties props,
            FilesystemModelScanner scanner,
            ModelImportServiceImpl.LibraryGateway libraryGateway,
            ModelImportServiceImpl.ModelGateway modelGateway,
            ModelImportServiceImpl.StlFileGateway stlGateway,
            ModelImportServiceImpl.ArtistGateway artistGateway,
            ModelImportServiceImpl.VariantGateway variantGateway,
            PlatformTransactionManager txManager
    ) {
        return new ModelImportServiceImpl(props, scanner, libraryGateway, modelGateway, stlGateway, artistGateway, variantGateway, new TransactionTemplate(txManager));
    }

    @Bean
    StartupModelImportRunner startupModelImportRunner(ModelImportProperties props, ModelImportService importService) {
        return new StartupModelImportRunner(props, importService);
    }

    // Optional: periodic rescan
    @Bean
    @ConditionalOnProperty(prefix = "importer.models", name = "schedule-enabled", havingValue = "true")
    TaskScheduler modelImportTaskScheduler(ModelImportProperties props, ModelImportService importService) {
        ThreadPoolTaskScheduler ts = new ThreadPoolTaskScheduler();
        ts.setPoolSize(1);
        ts.setThreadNamePrefix("model-import-");
        ts.initialize();
        Duration interval = props.getScheduleInterval();
        ts.scheduleAtFixedRate(importService::runFullSync, interval);
        return ts;
    }
}
