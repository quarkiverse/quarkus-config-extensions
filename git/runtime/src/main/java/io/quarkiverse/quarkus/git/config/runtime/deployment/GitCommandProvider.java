package io.quarkiverse.quarkus.git.config.runtime.deployment;

import java.io.File;

import org.eclipse.jgit.api.CloneCommand;

import io.quarkiverse.quarkus.git.config.runtime.config.GitAuthentication;

public interface GitCommandProvider {
    CloneCommand createCloneCommand(String uri, String tag, GitAuthentication auth, File stageDir);
}
