package com.github.kshashov.timetracker.web;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = {TimeTrackerApplication.class})
public abstract class BaseIntegrationTest {
}
