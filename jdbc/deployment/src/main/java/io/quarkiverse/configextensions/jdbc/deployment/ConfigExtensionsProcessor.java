package io.quarkiverse.configextensions.jdbc.deployment;

import io.quarkiverse.configextensions.jdbc.runtime.JdbcConfigConfig;
import io.quarkiverse.configextensions.jdbc.runtime.JdbcConfigRecorder;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.RunTimeConfigurationSourceValueBuildItem;

class ConfigExtensionsProcessor {

    private static final String FEATURE = "config-jdbc";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    RunTimeConfigurationSourceValueBuildItem initFakeConfig(JdbcConfigRecorder recorder, JdbcConfigConfig config) {
        return new RunTimeConfigurationSourceValueBuildItem(
                recorder.create(config));
    }

}
