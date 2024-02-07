package io.quarkiverse.quarkus.git.config.runtime.config;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

@ConfigGroup
public class SSHCredentials {

    /**
     * SSH identity: the private key.
     */
    @ConfigItem
    public String privateKey;

    /**
     * SSH identity: the public key.
     */
    @ConfigItem
    public String publicKey;

    /**
     * SSH identity: the passphrase to use to access the private key.
     */
    @ConfigItem
    public String passphrase;
}
