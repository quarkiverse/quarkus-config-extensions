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

public class GitMinimalConfigSourceTest {

    private static final String TEST_NAME = "minimal";

    public static final Path TEST_REPO_ROOT = Path.of("/tmp/git-config-source/tests");

    private static final Runnable BEFORE_ALL = () -> {
        var repoPath = TEST_REPO_ROOT.resolve(TEST_NAME);
        GitRepository.free(repoPath);
        try {
            Files.createDirectories(repoPath);
            var sourceURL = Thread.currentThread().getContextClassLoader().getResource("remote-application.properties");
            GitRepository.of(repoPath).init().addFile(new File(sourceURL.toURI()), "application.properties");
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
                        .addAsResource("remote-application.properties");
            });

    @AfterAll
    public static void afterAll() throws IOException {
        Path repoPath = TEST_REPO_ROOT.resolve(TEST_NAME);
        GitRepository.free(repoPath);
    }

    @Test
    public void testTheConfigValue() {
        var cfg = ConfigProvider.getConfig().getOptionalValue("a.test.key", String.class);
        assertThat(cfg).isNotEmpty().contains("a test value");
    }
}
