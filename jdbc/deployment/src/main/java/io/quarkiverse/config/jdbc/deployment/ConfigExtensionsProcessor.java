package io.quarkiverse.config.jdbc.deployment;

import io.quarkiverse.config.jdbc.runtime.JdbcConfigConfig;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class ConfigExtensionsProcessor {

    private static final String FEATURE = "config-jdbc";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void initFakeConfig(JdbcConfigConfig config) {
    }

}
