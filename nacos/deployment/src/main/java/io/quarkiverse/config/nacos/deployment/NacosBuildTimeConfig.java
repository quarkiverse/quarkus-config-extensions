package io.quarkiverse.config.nacos.deployment;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigDocSection;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.config.source.nacos")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface NacosBuildTimeConfig {
    /**
     * Dev Services allows Quarkus to automatically start Nacos in dev and test mode.
     */
    @ConfigDocSection(generated = true)
    DevServices devservices();

    interface DevServices {
        /**
         * If DevServices has been explicitly enabled or disabled. DevServices is generally enabled
         * by default, unless there is an existing configuration present.
         * <p>
         * When DevServices is enabled Quarkus will attempt to automatically configure and start
         * Nacos when running in Dev or Test mode and when Docker is running.
         */
        @WithDefault("true")
        boolean enabled();

        /**
         * The container image name to use.
         */
        Optional<String> imageName();

        /**
         * The configuration to load into Nacos.
         * <p>
         * Pre-loads the Nacos DevService container with some configuration.
         */
        LoadConfig loadConfig();

        interface LoadConfig {
            /**
             * The Nacos namespace.
             */
            @WithDefault("public")
            String namespace();

            /**
             * The Nacos configuration name.
             */
            @WithDefault("quarkus")
            String dataId();

            /**
             * The Nacos configuration group.
             */
            @WithDefault("quarkus")
            String group();

            /**
             * The configuration to load in Properties format.
             */
            @WithDefault("empty=")
            String content();
        }
    }
}
