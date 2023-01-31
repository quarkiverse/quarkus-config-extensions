package io.quarkus.config.hocon.runtime;

import io.quarkus.runtime.configuration.ConfigBuilder;
import io.smallrye.config.SmallRyeConfigBuilder;

public class HoconConfigBuilder implements ConfigBuilder {
    @Override
    public SmallRyeConfigBuilder configBuilder(SmallRyeConfigBuilder builder) {
        return builder
                .withSources(new ApplicationHoconConfigSourceLoader.InFileSystem())
                .withSources(new ApplicationHoconConfigSourceLoader.InClassPath());
    }
}
