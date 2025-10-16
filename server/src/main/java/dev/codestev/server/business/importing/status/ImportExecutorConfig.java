package dev.codestev.server.business.importing.status;// Java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ImportExecutorConfig {
    @Bean("ImportTaskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setThreadNamePrefix("import-");
        exec.setCorePoolSize(1);
        exec.setMaxPoolSize(1); // ensure only one sync at a time
        exec.initialize();
        return exec;
    }
}