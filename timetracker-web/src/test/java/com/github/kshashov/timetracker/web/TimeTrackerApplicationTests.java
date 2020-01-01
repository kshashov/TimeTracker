package com.github.kshashov.timetracker.web;

import com.github.kshashov.timetracker.core.i18n.Translator;
import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {TimeTrackerApplication.class})
class TimeTrackerApplicationTests {
    private static final Logger logger = LoggerFactory.getLogger(TimeTrackerApplicationTests.class);

    @Autowired
    Environment env;
    @Autowired
    @Qualifier("timeTrackerStringEncryptor")
    StringEncryptor encryptor;
    @Autowired
    Translator translator;

    @Test
    void testLogger() {
        logger.trace("trace");
        logger.info("info");
        logger.debug("debug");
        logger.warn("warn");
        logger.error("error");
    }

    @Test
    void testDecrypt() {
        var decrypted = env.getProperty("secret.property");
        Assertions.assertEquals("test", decrypted);
    }

    @Test
    void testI18n() {
        assertThat(translator.toLocale("lang", Locale.ENGLISH)).isEqualTo("en");
        assertThat(translator.toLocale("lang", new Locale("ru"))).isEqualTo("ru");
    }
}
