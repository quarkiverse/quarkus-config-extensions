package io.quarkiverse.quarkus.git.config.runtime.config;

import java.util.Optional;
import java.util.Set;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public class GitConfigSourceConfiguration {

    public static final String EXTENSION_NAME = "git-config-source";

    /**
     * Master switch to enable the Git config source.
     */
    @ConfigItem(defaultValue = "true")
    public boolean enabled;

    /**
     * Ordinal to determine the config source priority, see
     * {@link https://quarkus.io/guides/config-reference#configuration-sources}.
     */
    @ConfigItem(defaultValue = "500")
    public int ordinal;

    /**
     * The URI to clone from.
     */
    @ConfigItem
    public String uri;

    /**
     * The initial branch to check out when cloning the repository.
     * Can be specified as ref name (<code>refs/heads/master</code>),
     * branch name (<code>master</code>) or tag name
     * (<code>v1.2.3</code>). The default is to use the branch
     * pointed to by the cloned repository's HEAD and can be
     * requested by passing {@code null} or <code>HEAD</code>.
     */
    @ConfigItem
    public Optional<String> tag;

    /**
     * Configuration source files.
     */
    @ConfigItem(defaultValue = "application.properties")
    public Set<String> propertyFiles;

    /**
     * JGit authentication criteria
     */
    @ConfigItem
    public GitAuthentication authentication;
}
