package io.quarkus.consul.config.runtime;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.quarkus.runtime.configuration.DurationConverter;
import io.quarkus.runtime.configuration.InetSocketAddressConverter;
import io.quarkus.runtime.configuration.PathConverter;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithConverter;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.consul-config")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface ConsulConfig {

    /**
     * If set to true, the application will attempt to look up the configuration from Consul
     */
    @WithDefault("false")
    boolean enabled();

    /**
     * Consul agent related configuration
     */
    AgentConfig agent();

    /**
     * Common prefix that all keys share when looking up the keys from Consul.
     * The prefix is <b>not</b> included in the keys of the user configuration
     */
    Optional<String> prefix();

    /**
     * Keys whose value is a raw string.
     * When this is used, the keys that end up in the user configuration are the keys specified her with '/' replaced by '.'
     */
    Optional<List<String>> rawValueKeys();

    /**
     * Keys whose value represents a properties file.
     * When this is used, the keys that end up in the user configuration are the keys of the properties file,
     * not these keys
     */
    Optional<List<String>> propertiesValueKeys();

    /**
     * If set to true, the application will not start if any of the configured config sources cannot be located
     */
    @WithDefault("true")
    boolean failOnMissingKey();

    @ConfigGroup
    interface AgentConfig {

        /**
         * Consul agent host
         */
        @WithDefault("localhost:8500")
        @WithConverter(InetSocketAddressConverter.class)
        InetSocketAddress hostPort();

        /**
         * Whether or not to use HTTPS when communicating with the agent
         */
        @WithDefault("false")
        boolean useHttps();

        /**
         * Consul token to be provided when authentication is enabled
         */
        Optional<String> token();

        /**
         * TrustStore to be used containing the SSL certificate used by Consul agent
         * Can be either a classpath resource or a file system path
         */
        @WithConverter(PathConverter.class)
        Optional<Path> trustStore();

        /**
         * Password of TrustStore to be used containing the SSL certificate used by Consul agent
         */
        Optional<String> trustStorePassword();

        /**
         * KeyStore to be used containing the SSL certificate for authentication with Consul agent
         * Can be either a classpath resource or a file system path
         */
        @WithConverter(PathConverter.class)
        Optional<Path> keyStore();

        /**
         * Password of KeyStore to be used containing the SSL certificate for authentication with Consul agent
         */
        Optional<String> keyStorePassword();

        /**
         * Password to recover key from KeyStore for SSL client authentication with Consul agent
         * If no value is provided, the key-store-password will be used
         */
        Optional<String> keyPassword();

        /**
         * When using HTTPS and no keyStore has been specified, whether or not to trust all certificates
         */
        @WithDefault("false")
        boolean trustCerts();

        /**
         * The amount of time to wait when initially establishing a connection before giving up and timing out.
         * <p>
         * Specify `0` to wait indefinitely.
         */
        @WithDefault("10s")
        @WithConverter(DurationConverter.class)
        Duration connectionTimeout();

        /**
         * The amount of time to wait for a read on a socket before an exception is thrown.
         * <p>
         * Specify `0` to wait indefinitely.
         */
        @WithDefault("60s")
        @WithConverter(DurationConverter.class)
        Duration readTimeout();
    }
}
