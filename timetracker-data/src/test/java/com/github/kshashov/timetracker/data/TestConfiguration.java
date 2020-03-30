package com.github.kshashov.timetracker.data;

import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.github.kshashov.timetracker.data")
public class TestConfiguration {
    @Bean
    public CacheManager cacheManager() {
        return new NoOpCacheManager();
    }
}
