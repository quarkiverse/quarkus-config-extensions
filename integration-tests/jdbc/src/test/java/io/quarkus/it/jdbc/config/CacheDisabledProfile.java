package io.quarkus.it.jdbc.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.quarkus.test.junit.QuarkusTestProfile;

public class CacheDisabledProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("quarkus.config.source.jdbc.cache", "false");
    }

    @Override
    public Set<Class<?>> getEnabledAlternatives() {
        return QuarkusTestProfile.super.getEnabledAlternatives();
    }

    @Override
    public String getConfigProfile() {
        return QuarkusTestProfile.super.getConfigProfile();
    }

    @Override
    public List<TestResourceEntry> testResources() {
        return QuarkusTestProfile.super.testResources();
    }

    @Override
    public boolean disableGlobalTestResources() {
        return QuarkusTestProfile.super.disableGlobalTestResources();
    }

    @Override
    public Set<String> tags() {
        return QuarkusTestProfile.super.tags();
    }

    @Override
    public String[] commandLineParameters() {
        return QuarkusTestProfile.super.commandLineParameters();
    }

    @Override
    public boolean runMainMethod() {
        return QuarkusTestProfile.super.runMainMethod();
    }

    @Override
    public boolean disableApplicationLifecycleObservers() {
        return QuarkusTestProfile.super.disableApplicationLifecycleObservers();
    }
}
