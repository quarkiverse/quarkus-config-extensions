package io.quarkiverse.configextensions.jdbc.runtime;

import java.util.Collections;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class JdbcConfigRecorder {

    public RuntimeValue<ConfigSourceProvider> create(JdbcConfigConfig config) {
        return emptyRuntimeValue();
    }

    private RuntimeValue<ConfigSourceProvider> emptyRuntimeValue() {
        return new RuntimeValue<>(new EmptyConfigSourceProvider());
    }

    private static class EmptyConfigSourceProvider implements ConfigSourceProvider {

        @Override
        public Iterable<ConfigSource> getConfigSources(ClassLoader forClassLoader) {
            return Collections.emptyList();
        }
    }
}
