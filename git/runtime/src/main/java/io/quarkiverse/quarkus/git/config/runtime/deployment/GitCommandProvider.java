package io.quarkiverse.quarkus.git.config.runtime.deployment;

import java.io.File;

import org.eclipse.jgit.api.CloneCommand;

public interface GitCommandProvider {
    CloneCommand createCloneCommand(String uri, String tag, File stageDir);
}
