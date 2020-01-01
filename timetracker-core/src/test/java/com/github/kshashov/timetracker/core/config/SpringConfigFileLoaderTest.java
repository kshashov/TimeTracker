package com.github.kshashov.timetracker.core.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpringConfigFileLoaderTest {

    @Test
    void testExternalProperties() {
        var fileLoader = new SpringConfigFileLoader();

        assertDoesNotThrow(() -> {
            var properties = fileLoader.load(new ClassPathResource("test.txt").getFile().getAbsolutePath());
            assertEquals(1, properties.size());
            assertEquals("value", properties.get("key"));
        });

        assertDoesNotThrow(() -> {
            var resource = new ClassPathResource("test.txt");
            var properties = fileLoader.load(resource);
            assertEquals(1, properties.size());
            assertEquals("value", properties.get("key"));
        });
    }
}
