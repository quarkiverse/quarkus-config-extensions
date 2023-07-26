package io.quarkiverse.quarkus.git.config.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import io.quarkiverse.quarkus.git.config.runtime.GitConfigBuilder.GitConfigSource;
import io.smallrye.config.SmallRyeConfigBuilder;

@ExtendWith(MockitoExtension.class)
public class GitConfigBuilderTest {

    @Spy
    SmallRyeConfigBuilder smallRyeBuilder;

    @Captor
    ArgumentCaptor<List<ConfigSource>> captor;

    @Test
    public void testConfigSourceBuilder() {
        var builder = new GitConfigBuilder();
        builder.configBuilder(smallRyeBuilder);

        verify(smallRyeBuilder, only()).withSources(captor.capture());
        assertThat(captor.getValue()).isNotEmpty().hasExactlyElementsOfTypes(GitConfigSource.class);
    }
}
