package io.quarkiverse.quarkus.git.config.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.quarkus.git.config.test.resources.GitRepository;
import io.quarkus.test.QuarkusUnitTest;

public class GitMultiConfigSourceTest {

    private static final String TEST_NAME = "multi";

    public static final Path TEST_REPO_ROOT = Path.of("/tmp/git-config-source/tests");

    private static final Runnable BEFORE_ALL = () -> {
        var repoPath = TEST_REPO_ROOT.resolve(TEST_NAME);
        GitRepository.free(repoPath);
        try {
            Files.createDirectories(repoPath);
            var sourceURL1 = Thread.currentThread().getContextClassLoader()
                    .getResource("remote-multi-application-1.properties");
            var sourceURL2 = Thread.currentThread().getContextClassLoader()
                    .getResource("remote-multi-application-2.properties");
            GitRepository.of(repoPath).init()
                    .addFile(new File(sourceURL1.toURI()), "remote-multi-application-1.properties")
                    .addFile(new File(sourceURL2.toURI()), "remote-multi-application-2.properties");
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    };

    // Start unit test with your extension loaded
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .withConfigurationResource(TEST_NAME + "-application.properties")
            .setBeforeAllCustomizer(BEFORE_ALL)
            .setArchiveProducer(() -> {
                return ShrinkWrap.create(JavaArchive.class)
                        .addClass(GitRepository.class)
                        .addAsResource("remote-multi-application-1.properties")
                        .addAsResource("remote-multi-application-2.properties");
            });

    @AfterAll
    public static void afterAll() throws IOException {
        Path repoPath = TEST_REPO_ROOT.resolve(TEST_NAME);
        GitRepository.free(repoPath);
    }

    @Test
    public void testTheConfigValue() {
        var cfg = ConfigProvider.getConfig();

        var sharedVal = cfg.getOptionalValue("a.test.key", String.class);
        assertThat(sharedVal).isNotEmpty().contains("a test value");

        var val1 = cfg.getOptionalValue("a.test.key.1", String.class);
        assertThat(val1).isNotEmpty().contains("a test value 1");

        var val2 = cfg.getOptionalValue("a.test.key.2", String.class);
        assertThat(val2).isNotEmpty().contains("a test value 2");
    }
}
