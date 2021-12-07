package io.quarkus.consul.config;

import org.jsoup.Connection.Response;

import io.quarkus.consul.config.runtime.ConsulConfigRecorder;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.RunTimeConfigurationSourceValueBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;

public class ConsulConfigProcessor {

    private static final String FEATURE = "consul-config";

    @BuildStep
    public void feature(BuildProducer<FeatureBuildItem> feature) {
        feature.produce(new FeatureBuildItem(FEATURE));
    }

    @BuildStep
    public void enableSsl(BuildProducer<ExtensionSslNativeSupportBuildItem> extensionSslNativeSupport) {
        extensionSslNativeSupport.produce(new ExtensionSslNativeSupportBuildItem(FEATURE));
    }

    @BuildStep
    public void registerForReflection(BuildProducer<ReflectiveClassBuildItem> reflectiveClass) {
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, false, Response.class));
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    public RunTimeConfigurationSourceValueBuildItem configure(ConsulConfigRecorder recorder) {
        return new RunTimeConfigurationSourceValueBuildItem(
                recorder.configSources());
    }

}
