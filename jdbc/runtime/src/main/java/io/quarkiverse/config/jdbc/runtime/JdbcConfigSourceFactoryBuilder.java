package io.quarkiverse.config.jdbc.runtime;

import io.quarkus.runtime.configuration.ConfigBuilder;
import io.smallrye.config.SmallRyeConfigBuilder;

public class JdbcConfigSourceFactoryBuilder implements ConfigBuilder {
    @Override
    public SmallRyeConfigBuilder configBuilder(final SmallRyeConfigBuilder builder) {
        builder.withSources(new JdbcConfigSourceFactory());
        return builder;
    }
}
