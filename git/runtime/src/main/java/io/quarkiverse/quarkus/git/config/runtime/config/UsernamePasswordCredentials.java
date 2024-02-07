package io.quarkiverse.quarkus.git.config.runtime.config;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

@ConfigGroup
public class UsernamePasswordCredentials {

    /**
     * Username to connect to the GIT repository.
     */
    @ConfigItem
    public String username;

    /**
     * Password to connect to the GIT repository.
     */
    @ConfigItem
    public String password;
}
