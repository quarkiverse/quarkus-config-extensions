package io.quarkus.config.hocon.deployment;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.config.ConfigProvider;

import io.quarkus.config.hocon.runtime.HoconConfigBuilder;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.*;
import io.smallrye.config.SmallRyeConfig;

public final class ConfigHoconProcessor {

    private static final String FEATURE = "hocon-config";

    @BuildStep
    public void feature(BuildProducer<FeatureBuildItem> feature) {
        feature.produce(new FeatureBuildItem(FEATURE));
    }

    @BuildStep
    public void hoconConfig(BuildProducer<StaticInitConfigBuilderBuildItem> staticInitConfigBuilder,
            BuildProducer<RunTimeConfigBuilderBuildItem> runTimeConfigBuilder) {
        staticInitConfigBuilder.produce(
                new StaticInitConfigBuilderBuildItem(HoconConfigBuilder.class.getName()));
        runTimeConfigBuilder.produce(
                new RunTimeConfigBuilderBuildItem(HoconConfigBuilder.class.getName()));
    }

    @BuildStep
    void watchHoconConfig(BuildProducer<HotDeploymentWatchedFileBuildItem> watchedFiles) {
        List<String> configWatchedFiles = new ArrayList<>();
        String userDir = System.getProperty("user.dir");

        // Main files
        configWatchedFiles.add("application.conf");
        configWatchedFiles.add("application.conf");
        configWatchedFiles.add(Paths.get(userDir, "config", "application.conf").toAbsolutePath().toString());
        configWatchedFiles.add(Paths.get(userDir, "config", "application.conf").toAbsolutePath().toString());

        // Profiles
        SmallRyeConfig config = ConfigProvider.getConfig().unwrap(SmallRyeConfig.class);
        for (String profile : config.getProfiles()) {
            configWatchedFiles.add(String.format("application-%s.conf", profile));
            configWatchedFiles.add(String.format("application-%s.conf", profile));
            configWatchedFiles.add(
                    Paths.get(userDir, "config", String.format("application-%s.conf", profile)).toAbsolutePath().toString());
            configWatchedFiles.add(
                    Paths.get(userDir, "config", String.format("application-%s.conf", profile)).toAbsolutePath().toString());
        }

        for (String configWatchedFile : configWatchedFiles) {
            watchedFiles.produce(new HotDeploymentWatchedFileBuildItem(configWatchedFile));
        }
    }
}
