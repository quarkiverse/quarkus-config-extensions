package io.quarkiverse.configextensions.jdbc.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

/**
 * We don't really use this, because these are configurations for the config itself, so it causes a chicken / egg
 * problem, but we have it so the configurations can be properly documented.
 *
 * The config itself is loaded using the ConfigSourceContext on the ConfigSourceFactory
 */
@ConfigRoot(name = "config.source.jdbc", phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public class JdbcConfigConfig {

    /**
     * If set to true, the application will attempt to look up the configuration from DB
     */
    @ConfigItem(name = "enabled", defaultValue = "true")
    public boolean enabled;

    /**
     * Table name for configuration records
     */
    @ConfigItem(name = "table", defaultValue = "configuration")
    public Optional<String> table;

    /**
     * Name of the column containing the key
     */
    @ConfigItem(name = "key", defaultValue = "key")
    public Optional<String> keyColumn;

    /**
     * name of the column containing the value
     */
    @ConfigItem(name = "value", defaultValue = "value")
    public Optional<String> valueColumn;
}
