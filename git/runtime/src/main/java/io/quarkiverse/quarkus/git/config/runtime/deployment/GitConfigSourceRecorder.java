package io.quarkiverse.quarkus.git.config.runtime.deployment;

import static java.util.Collections.emptyMap;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeSet;

import jakarta.inject.Inject;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.TagOpt;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

import io.quarkiverse.quarkus.git.config.runtime.GitConfigBuilder.GitConfigSource;
import io.quarkiverse.quarkus.git.config.runtime.config.GitAuthentication;
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
                LOG.error("Cannot create the stage directory", e);
                return Optional.empty();
            }
        }

        @Override
        public Properties read(Path configContent) {
            Properties props = new Properties();
            try (var reader = new FileReader(configContent.toFile())) {
                props.load(reader);
            } catch (IOException e) {
                LOG.error(String.format("Cannot load properties from [%s]", configContent), e);
            }
            return props;
        }
    };

    private static final GitCommandProvider DEFAULT_GIT_PROVIDER = new GitCommandProvider() {

        @Override
        public CloneCommand createCloneCommand(String uri, String tag, GitAuthentication auth, File stageDir) {
            var rv = Git.cloneRepository();

            if (auth.credentials.isPresent()) {
                var usernamePasswordCredentials = auth.credentials.get();
                rv.setCredentialsProvider(new UsernamePasswordCredentialsProvider(usernamePasswordCredentials.username,
                        usernamePasswordCredentials.password));
            }

            if (auth.keys.isPresent()) {
                var keysCredentials = auth.keys.get();
                var sshSessionFactory = createSshSessionFactory(keysCredentials.privateKey,
                        keysCredentials.publicKey, keysCredentials.passphrase);

                rv.setTransportConfigCallback(transport -> {
                    if (transport instanceof SshTransport) {
                        ((SshTransport) transport).setSshSessionFactory(sshSessionFactory);
                    }
                });
            }

            return rv
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

        private SshSessionFactory createSshSessionFactory(String privateKey, String publicKey, String passphrase) {
            return new JschConfigSessionFactory() {
                @Override
                protected void configureJSch(JSch jsch) {
                    try {
                        jsch.addIdentity(GitConfigSourceConfiguration.EXTENSION_NAME,
                                privateKey.getBytes(StandardCharsets.UTF_8), publicKey.getBytes(StandardCharsets.UTF_8),
                                passphrase.getBytes(StandardCharsets.UTF_8));
                    } catch (JSchException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
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

    public void init(GitConfigSourceConfiguration config) {
        var remoteProperties = readRemoteRepository();
        GitConfigSource.init(remoteProperties, config.ordinal);
    }

    private Map<String, String> readRemoteRepository() {

        Optional<File> tmpDir = fsProvider.createStageDir();
        if (tmpDir.isEmpty()) {
            LOG.warn("Failed to initialize the git config source");
            return emptyMap();
        }
        var stageDir = tmpDir.get();
        var cloneCommand = gitCommandProvider.createCloneCommand(config.uri, config.tag.orElse("HEAD"),
                config.authentication, stageDir);
        try (var git = cloneCommand.call()) {
            var rv = new HashMap<String, String>();
            rv.putAll(loadProperties(stageDir));
            loadYaml(stageDir).forEach((name, value) -> {
                if (rv.containsKey(name)) {
                    LOG.warn("Overlapping YAML entry with key [{}], overriding with the latest...", name);
                }
                rv.put(name, value);
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

    private Map<String, String> loadProperties(File stageDir) {
        var rv = new HashMap<String, String>();
        for (var useFile : new TreeSet<>(config.propertyFiles)) {
            var props = fsProvider.read(stageDir.toPath().resolve(useFile));
            props.stringPropertyNames().forEach(name -> {
                if (rv.containsKey(name)) {
                    LOG.warn("Duplicated entry with key [{}] in [{}], overriding with the latest...", name, useFile);
                }
                rv.put(name, props.getProperty(name));
            });
        }
        return rv;
    }

    private Map<String, String> loadYaml(File stageDir) {
        var rv = new HashMap<String, String>();
        // for (var useFile : new TreeSet<>(config.yamlFiles)) {
        // var props = fsProvider.read(stageDir.toPath().resolve(useFile));
        // props.stringPropertyNames().forEach(name -> {
        // if (rv.containsKey(name)) {
        // LOG.warn("Duplicated entry with key [{}] in [{}], skipping...",
        // name, useFile);
        // }
        // rv.putIfAbsent(name, props.getProperty(name));
        // });
        // }
        return rv;
    }
}
