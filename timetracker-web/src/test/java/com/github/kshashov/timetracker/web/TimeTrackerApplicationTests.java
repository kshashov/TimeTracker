package com.github.kshashov.timetracker.web;

import com.github.kshashov.timetracker.core.i18n.Translator;
import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {"secret.property=ENC(RH9JweLibTm0+4Tukivw4Y11qukSqAabrKukgpW0zcQWg10PIs0aR79a3YhqCPu9)"})
class TimeTrackerApplicationTests extends BaseIntegrationTest {
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
    void testI18n() {
        assertThat(translator.toLocale("lang", Locale.ENGLISH)).isEqualTo("en");
        assertThat(translator.toLocale("lang", new Locale("ru"))).isEqualTo("ru");
    }

    @Test
    void testEncrypt() {
        var decrypted = env.getProperty("secret.property");
        Assertions.assertEquals("test", decrypted);
    }
}
