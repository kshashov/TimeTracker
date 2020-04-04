package com.github.kshashov.timetracker.data;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@ActiveProfiles("test")
@DirtiesContext
@ContextConfiguration(classes = MainTestConfiguration.class)
public class BaseTest {
}
