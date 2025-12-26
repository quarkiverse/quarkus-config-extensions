package io.quarkiverse.config.nacos;

import static io.quarkus.runtime.annotations.ConfigPhase.RUN_TIME;

import java.net.InetSocketAddress;
import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigRoot;
import io.quarkus.runtime.configuration.InetSocketAddressConverter;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithConverter;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.config.source.nacos")
@ConfigRoot(phase = RUN_TIME)
public interface NacosConfig {
    /**
     * The Nacos address.
     */
    @WithConverter(InetSocketAddressConverter.class)
    InetSocketAddress serverAddr();

    /**
     * The Nacos username.
     */
    Optional<String> username();

    /**
     * The Nacos password.
     */
    Optional<String> password();

    /**
     * The Nacos namespace.
     */
    @WithDefault("public")
    String namespace();

    /**
     * The Nacos configuration name.
     */
    String dataId();

    /**
     * The Nacos configuration group.
     */
    @WithDefault("DEFAULT_GROUP")
    String group();
}
