package io.quarkiverse.config.jdbc.runtime;

import java.time.Duration;
import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.quarkus.runtime.configuration.DurationConverter;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithConverter;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * We don't really use this, because these are configurations for the config itself, so it causes a chicken / egg
 * problem, but we have it so the configurations can be properly documented.
 * <br>
 * The config itself is loaded using the ConfigSourceContext on the ConfigSourceFactory
 */
@ConfigMapping(prefix = "quarkus.config.source.jdbc")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface JdbcConfigConfig {

    /**
     * If set to true, the application will attempt to look up the configuration from DB
     */
    @WithDefault("true")
    boolean enabled();

    /**
     * If set to true, the application will cache all looked up the configuration from DB in memory
     * If set to false, the application will always get the latest values from the DB
     */
    @WithDefault("true")
    boolean cache();

    /**
     * Table name for configuration records
     */
    @WithDefault("configuration")
    String table();

    /**
     * Name of the column containing the key
     */
    @WithName("key")
    @WithDefault("configuration_key")
    String keyColumn();

    /**
     * Name of the column containing the value
     */
    @WithName("value")
    @WithDefault("configuration_value")
    String valueColumn();

    /**
     * The datasource username, if not defined the username of the default datasource is used
     */
    @WithDefault("${quarkus.datasource.username}")
    Optional<String> username();

    /**
     * The datasource password, if not defined the password of the default datasource is used
     */
    @WithDefault("${quarkus.datasource.password}")
    Optional<String> password();

    /**
     * The datasource URL, if not defined the URL of the default datasource is used
     */
    @WithDefault("${quarkus.datasource.jdbc.url}")
    String url();

    /**
     * The datasource driver, if not defined the driver of the default datasource is used
     */
    @WithDefault("${quarkus.datasource.jdbc.driver}")
    Optional<String> driver();

    /**
     * The initial size of the pool. Usually you will want to set the initial size to match at least the
     * minimal size, but this is not enforced so to allow for architectures which prefer a lazy initialization
     * of the connections on boot, while being able to sustain a minimal pool size after boot.
     */
    @WithDefault("0")
    int initialSize();

    /**
     * The datasource pool minimum size
     */
    @WithDefault("0")
    int minSize();

    /**
     * The datasource pool maximum size
     */
    @WithDefault("5")
    int maxSize();

    /**
     * The timeout before cancelling the acquisition of a new connection
     */
    @WithDefault("5")
    @WithConverter(DurationConverter.class)
    Duration acquisitionTimeout();
}
