package io.quarkiverse.config.jdbc.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class ConfigExtensionsProcessor {

    private static final String FEATURE = "config-extensions";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }
}
