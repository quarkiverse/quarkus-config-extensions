package io.quarkiverse.quarkus.git.config.runtime.config;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

@ConfigGroup
public class GitAuthentication {

    /**
     * Username and password credentials.
     */
    @ConfigItem
    public Optional<UsernamePasswordCredentials> credentials;

    /**
     * SSH Identity.
     */
    @ConfigItem
    public Optional<SSHCredentials> keys;
}
