package com.github.kshashov.timetracker.core.utils;

import com.google.common.base.Strings;
import lombok.NonNull;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ResourceUtils {

    /**
     * Find first filename that exists.
     *
     * @param filenames Names of files to search for
     * @return Optional file path found
     */
    public static Optional<Path> findFirstFilesystemResource(Collection<String> filenames, Collection<Path> systemPaths) {
        return findFilesystemResources(filenames, systemPaths).stream().findFirst();
    }

    /**
     * Finds existing files in the filesystem resource path of the specified names.
     *
     * @param filenames Names of files to search for
     * @return List of Paths for only those filenames that were found
     */
    public static List<Path> findFilesystemResources(Collection<String> filenames, Collection<Path> systemPaths) {
        List<Path> paths = new ArrayList<>();
        for (Path path : systemPaths) {
            for (String filename : filenames) {
                final Path file = path.resolve(filename);
                if (Files.exists(file)) {
                    paths.add(file);
                }
            }
        }
        return paths;
    }


    /**
     * Find the named resources:
     * 1. by property name: `-DPROPERTY_NAME=filename`
     * 2. by environment variable: `export ENV_VAR_NAME=filename`
     * 3. by file name
     */
    public static Resource findNamedResource(@NonNull final Environment env, @NonNull final String propertyName, @NonNull final String environmentName, @NonNull final String fileName) {
        return findNamedResource(env, propertyName)
                .or(() -> findNamedResource(env, environmentName))
                .orElse(findNamedResource(fileName));
    }

    /**
     * Find the named resource by property name.
     */
    public static Optional<Resource> findNamedResource(@NonNull final Environment env, @NonNull final String propertyName) {
        final String filename = env.getProperty(propertyName, "");

        if (!Strings.isNullOrEmpty(filename))
            return Optional.of(findNamedResource(filename));

        return Optional.empty();
    }

    /**
     * Find the named resource by file name.
     */
    public static Resource findNamedResource(@NonNull final String fileName) {
        return new FileSystemResource(fileName);
    }

    /**
     * Read a file into a string.
     */
    public static String readToString(Path path, Consumer<Throwable> onError) {
        try {
            return readToString(path);
        } catch (IOException e) {
            onError.accept(e);
            return null;
        }
    }

    /**
     * Read a file into a string.
     */
    public static String readToString(Path path) throws IOException {
        return Files.readString(path);
    }
}
