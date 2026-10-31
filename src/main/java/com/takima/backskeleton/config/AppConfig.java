package com.takima.backskeleton.config;

import com.takima.backskeleton.services.llm.LlmProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableConfigurationProperties(LlmProperties.class)
public class AppConfig {
    /** Les débats tournent en arrière-plan : le conseil finit même si le navigateur se ferme. */
    @Bean
    public ThreadPoolTaskExecutor debateExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(32);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("debate-");
        return executor;
    }

    /** Le fil du Greffier : séparé, pour que ses remarques ne prennent jamais la place d'un débat. */
    @Bean
    public ThreadPoolTaskExecutor scribeExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("scribe-");
        return executor;
    }
}
