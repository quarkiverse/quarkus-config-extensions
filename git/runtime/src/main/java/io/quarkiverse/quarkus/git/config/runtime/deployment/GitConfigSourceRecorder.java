package io.quarkiverse.quarkus.git.config.runtime.deployment;

import static java.util.Collections.emptyMap;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import jakarta.inject.Inject;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.TagOpt;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkiverse.quarkus.git.config.runtime.GitConfigBuilder.GitConfigSource;
import io.quarkiverse.quarkus.git.config.runtime.config.GitConfigSourceConfiguration;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class GitConfigSourceRecorder {

    private static final Logger LOG = LoggerFactory.getLogger(GitConfigSource.class);

    private static final FileSystemProvider DEFAULT_FS_PROVIDER = new FileSystemProvider() {

        @Override
        public Optional<File> createStageDir() {
            try {
                return Optional.of(Files.createTempDirectory("tmpgit").toFile());
            } catch (IOException e) {
                return Optional.empty();
            }
        }

        @Override
        public Properties read(Path configContent) {
            LOG.info("Reading [{}]", configContent);
            try (var reader = new FileReader(configContent.toFile())) {
                Properties props = new Properties();
                props.load(reader);
                return props;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private static final GitCommandProvider DEFAULT_GIT_PROVIDER = new GitCommandProvider() {

        @Override
        public CloneCommand createCloneCommand(String uri, String tag, File stageDir) {
            return Git.cloneRepository()
                    .setBranch(tag)
                    .setCloneAllBranches(true)
                    .setTagOption(TagOpt.FETCH_TAGS)
                    .setCloneSubmodules(true)
                    .setDirectory(stageDir)
                    .setDepth(1)
                    .setURI(uri)
                    .setFs(FS.detect())
                    .setRemote("origin");
        }
    };

    private GitConfigSourceConfiguration config;

    private FileSystemProvider fsProvider;

    private GitCommandProvider gitCommandProvider;

    @Inject
    public GitConfigSourceRecorder(GitConfigSourceConfiguration config) {
        this(config, DEFAULT_FS_PROVIDER, DEFAULT_GIT_PROVIDER);
    }

    public GitConfigSourceRecorder(GitConfigSourceConfiguration config, FileSystemProvider fsProvider,
            GitCommandProvider gitCommandProvider) {
        this.config = config;
        this.fsProvider = fsProvider;
        this.gitCommandProvider = gitCommandProvider;
    }

    public void init() {
        var remoteProperties = readRemoteRepository();
        GitConfigSource.properties.putAll(remoteProperties);
    }

    private Map<String, String> readRemoteRepository() {

        // https://dzone.com/articles/how-to-authenticate-with-jgit

        Optional<File> tmpDir = fsProvider.createStageDir();
        if (tmpDir.isEmpty()) {
            LOG.warn("Failed to initialize the git config source");
            return emptyMap();
        }
        var stageDir = tmpDir.get();
        var cloneCommand = gitCommandProvider.createCloneCommand(config.uri, config.tag.orElse("HEAD"), stageDir);
        try (var git = cloneCommand.call()) {
            var useFile = config.propertyFiles.iterator().next();
            LOG.info("Fetching file [{}] of [{}]", useFile, config.propertyFiles);

            // Just properties, todo yaml
            Properties props = fsProvider.read(stageDir.toPath().resolve(useFile));
            var rv = new HashMap<String, String>();
            props.stringPropertyNames().forEach(name -> {
                rv.put(name, props.getProperty(name));
            });
            return rv;
        } catch (Exception e) {
            LOG.warn("Failed to initialize the git config source", e);
            return emptyMap();
        } finally {
            try {
                Files.walk(stageDir.toPath())
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
            }
        }
    }
}
