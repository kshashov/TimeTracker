package com.github.kshashov.timetracker.core.config;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Properties;

public interface ConfigFileLoader {

    Properties load(final Resource resource) throws IOException;

    Properties load(final String fileName) throws IOException;
}
