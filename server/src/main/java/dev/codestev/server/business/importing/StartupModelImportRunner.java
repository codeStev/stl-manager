package dev.codestev.server.business.importing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

public class StartupModelImportRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(StartupModelImportRunner.class);

    private final ModelImportProperties props;
    private final ModelImportService importService;

    public StartupModelImportRunner(ModelImportProperties props, ModelImportService importService) {
        this.props = props;
        this.importService = importService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!props.isEnabled()) {
            log.info("Startup model import disabled");
            return;
        }
        importService.runFullSync();
    }
}
