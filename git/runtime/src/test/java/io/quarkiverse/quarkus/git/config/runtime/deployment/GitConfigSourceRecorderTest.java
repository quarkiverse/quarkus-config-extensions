package io.quarkiverse.quarkus.git.config.runtime.deployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import io.quarkiverse.quarkus.git.config.runtime.GitConfigBuilder.GitConfigSource;
import io.quarkiverse.quarkus.git.config.runtime.config.GitConfigSourceConfiguration;

@ExtendWith(MockitoExtension.class)
public class GitConfigSourceRecorderTest {
    @Mock
    FileSystemProvider fsProvider;
    @Mock
    GitCommandProvider gitProvider;
    @Mock
    Repository repository;

    @Spy
    CloneCommand cloneCommand;

    GitConfigSourceRecorder recorder;

    @BeforeEach
    public void beforeEach() {

        GitConfigSource.init(new ConcurrentHashMap<>(), 0);

        var cfg = new GitConfigSourceConfiguration();
        cfg.uri = "";
        cfg.tag = Optional.empty();
        cfg.propertyFiles = Set.of("application.properties");

        recorder = new GitConfigSourceRecorder(cfg, fsProvider, gitProvider);
    }

    @Test
    public void testFailToCreateStageDir() {
        doReturn(Optional.empty()).when(fsProvider).createStageDir();

        recorder.init(new GitConfigSourceConfiguration());

        verify(gitProvider, never()).createCloneCommand(anyString(), anyString(), any(File.class));
        verify(fsProvider, never()).read(any(Path.class));

        assertThat(GitConfigSource.getPropertyStore()).isEmpty();
    }

    @Test
    public void testFailToCloneTheRepository() throws InvalidRemoteException, TransportException, GitAPIException, IOException {
        defaultCreateStageDirStub();
        doReturn(cloneCommand).when(gitProvider).createCloneCommand(anyString(), anyString(), any(File.class));
        doThrow(new CanceledException("")).when(cloneCommand).call();

        recorder.init(new GitConfigSourceConfiguration());

        verify(fsProvider).createStageDir();
        verify(gitProvider).createCloneCommand(anyString(), anyString(), any(File.class));
        verify(fsProvider, never()).read(any(Path.class));

        assertThat(GitConfigSource.getPropertyStore()).isEmpty();
    }

    @Test
    public void testFailToReadThePropertyFile()
            throws InvalidRemoteException, TransportException, GitAPIException, IOException {
        defaultCreateStageDirStub();
        defaultCreateCloneCommandStub();

        doThrow(RuntimeException.class).when(fsProvider).read(any(Path.class));

        recorder.init(new GitConfigSourceConfiguration());

        verify(fsProvider).createStageDir();
        verify(gitProvider, only()).createCloneCommand(anyString(), anyString(), any(File.class));
        verify(fsProvider).read(any(Path.class));

        assertThat(GitConfigSource.getPropertyStore()).isEmpty();
    }

    @Test
    public void testConfigSourceInitialization()
            throws IOException, InvalidRemoteException, TransportException, GitAPIException {

        defaultCreateStageDirStub();
        defaultCreateCloneCommandStub();
        defaultReadPropertiesStub();

        recorder.init(new GitConfigSourceConfiguration());

        verify(fsProvider).createStageDir();
        verify(gitProvider, only()).createCloneCommand(anyString(), anyString(), any(File.class));
        verify(fsProvider).read(any(Path.class));

        assertThat(GitConfigSource.getPropertyStore()).isNotEmpty().containsOnlyKeys("a.test.key");
    }

    private void defaultCreateCloneCommandStub() throws GitAPIException, InvalidRemoteException, TransportException {
        doReturn(Git.wrap(repository)).when(cloneCommand).call();
        doReturn(cloneCommand).when(gitProvider).createCloneCommand(anyString(), anyString(), any(File.class));
    }

    private void defaultCreateStageDirStub() throws IOException {
        var tmp = File.createTempFile("test", "");
        tmp.deleteOnExit();
        doReturn(Optional.of(tmp)).when(fsProvider).createStageDir();
    }

    private void defaultReadPropertiesStub() {
        Properties props = new Properties();
        props.setProperty("a.test.key", "a test value");
        doReturn(props).when(fsProvider).read(any(Path.class));
    }
}
