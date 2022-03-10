package io.quarkiverse.config.jdbc.runtime;

import java.time.Duration;
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
    public boolean enabled = true;

    /**
     * If set to true, the application will cache all looked up the configuration from DB in memory
     * If set to false, the application will always get the latest values from the DB
     */
    @ConfigItem(name = "cache", defaultValue = "true")
    public boolean cache = true;

    /**
     * Table name for configuration records
     */
    @ConfigItem(name = "table", defaultValue = "configuration")
    public String table;

    /**
     * Name of the column containing the key
     */
    @ConfigItem(name = "key", defaultValue = "key")
    public String keyColumn;

    /**
     * Name of the column containing the value
     */
    @ConfigItem(name = "value", defaultValue = "value")
    public String valueColumn;

    /**
     * The datasource username, if not defined the username of the default datasource is used
     */
    @ConfigItem(name = "username")
    public Optional<String> username = Optional.empty();;

    /**
     * The datasource password, if not defined the password of the default datasource is used
     */
    @ConfigItem(name = "password")
    public Optional<String> password = Optional.empty();;

    /**
     * The datasource URL, if not defined the URL of the default datasource is used
     */
    @ConfigItem(name = "url")
    public Optional<String> url = Optional.empty();

    /**
     * The initial size of the pool. Usually you will want to set the initial size to match at least the
     * minimal size, but this is not enforced so to allow for architectures which prefer a lazy initialization
     * of the connections on boot, while being able to sustain a minimal pool size after boot.
     */
    @ConfigItem(name = "initial-size", defaultValue = "0")
    public int initialSize = 0;

    /**
     * The datasource pool minimum size
     */
    @ConfigItem(name = "min-size", defaultValue = "0")
    public int minSize = 0;

    /**
     * The datasource pool maximum size
     */
    @ConfigItem(name = "max-size", defaultValue = "5")
    public int maxSize = 5;

    /**
     * The timeout before cancelling the acquisition of a new connection
     */
    @ConfigItem(name = "acquisition-timeout", defaultValue = "5")
    public Duration acquisitionTimeout = Duration.ofSeconds(5);
}
