package com.github.kshashov.timetracker.core.config;

import com.github.kshashov.timetracker.core.utils.ResourceUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Properties;

@Service
public class SpringConfigFileLoader implements ConfigFileLoader {

    @Override
    public Properties load(final String fileName) throws IOException {
        Resource resource = ResourceUtils.findNamedResource(fileName);
        return PropertiesLoaderUtils.loadProperties(resource);
    }

    @Override
    public Properties load(final Resource resource) throws IOException {
        return PropertiesLoaderUtils.loadProperties(resource);
    }
}
