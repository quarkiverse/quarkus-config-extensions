package io.quarkiverse.quarkus.git.config.deployment;

import io.quarkiverse.quarkus.git.config.runtime.GitConfigBuilder;
import io.quarkiverse.quarkus.git.config.runtime.config.GitConfigSourceConfiguration;
import io.quarkiverse.quarkus.git.config.runtime.deployment.GitConfigSourceRecorder;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.RunTimeConfigBuilderBuildItem;

class GitConfigSourceProcessor {

    private static final String FEATURE = GitConfigSourceConfiguration.EXTENSION_NAME;

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    RunTimeConfigBuilderBuildItem configSource() {
        return new RunTimeConfigBuilderBuildItem(GitConfigBuilder.class);
    }

    @BuildStep
    @Record(value = ExecutionTime.RUNTIME_INIT)
    void initGitSource(GitConfigSourceRecorder recorder, GitConfigSourceConfiguration configuration) {
        recorder.init(configuration);
    }
}
