package io.quarkiverse.quarkus.git.config.runtime;

import static java.util.Collections.singletonList;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.microprofile.config.spi.ConfigSource;

import io.quarkus.runtime.configuration.ConfigBuilder;
import io.smallrye.config.SmallRyeConfigBuilder;

public class GitConfigBuilder implements ConfigBuilder {

    public static class GitConfigSource implements ConfigSource {

        public static Map<String, String> properties = new ConcurrentHashMap<>();

        @Override
        public int getOrdinal() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Set<String> getPropertyNames() {
            return properties.keySet();
        }

        @Override
        public String getValue(String propertyName) {
            return properties.get(propertyName);
        }

        @Override
        public String getName() {
            return GitConfigSource.class.getName();
        }
    }

    @Override
    public SmallRyeConfigBuilder configBuilder(SmallRyeConfigBuilder builder) {
        return builder.withSources(singletonList(new GitConfigSource()));
    }
}
