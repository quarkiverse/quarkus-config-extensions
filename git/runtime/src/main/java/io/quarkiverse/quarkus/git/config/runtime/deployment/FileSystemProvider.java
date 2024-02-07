package io.quarkiverse.quarkus.git.config.runtime.deployment;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

public interface FileSystemProvider {
    Optional<File> createStageDir();

    Properties read(Path configContent);
}
