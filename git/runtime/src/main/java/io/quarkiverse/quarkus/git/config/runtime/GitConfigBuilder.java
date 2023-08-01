package io.quarkiverse.quarkus.git.config.runtime;

import static java.util.Collections.singletonList;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.microprofile.config.spi.ConfigSource;

import io.quarkus.runtime.configuration.ConfigBuilder;
import io.smallrye.config.SmallRyeConfigBuilder;

public class GitConfigBuilder implements ConfigBuilder {

    public static class GitConfigSource implements ConfigSource {

        private static Map<String, String> properties = new ConcurrentHashMap<>();
        private static int ordinal;

        public static Map<String, String> init(Map<String, String> newValues, int ordinal) {
            var rv = new HashMap<>(properties);
            GitConfigSource.properties = new ConcurrentHashMap<>(newValues);
            GitConfigSource.ordinal = ordinal;
            return rv;
        }

        public static Map<String, String> getPropertyStore() {
            return new HashMap<>(properties);
        }

        @Override
        public int getOrdinal() {
            return GitConfigSource.ordinal;
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
