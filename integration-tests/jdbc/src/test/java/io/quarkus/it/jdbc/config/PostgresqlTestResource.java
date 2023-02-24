package io.quarkus.it.jdbc.config;

import java.util.Map;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class PostgresqlTestResource implements QuarkusTestResourceLifecycleManager {
    private PostgreSQLContainer container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:14"))
            .withPassword("sa")
            .withUsername("sa")
            .withInitScript("import.sql");

    @Override
    public Map<String, String> start() {
        container.start();

        return Map.of("quarkus.datasource.jdbc.url", container.getJdbcUrl());
    }

    @Override
    public void stop() {
        container.stop();
    }
}
