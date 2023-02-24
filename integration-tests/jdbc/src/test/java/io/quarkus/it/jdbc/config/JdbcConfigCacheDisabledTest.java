package io.quarkus.it.jdbc.config;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.equalTo;

import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import io.agroal.api.AgroalDataSource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@QuarkusTestResource(PostgresqlTestResource.class)
@TestProfile(CacheDisabledProfile.class)
public class JdbcConfigCacheDisabledTest {
    @Inject
    AgroalDataSource dataSource;

    @ConfigProperty(name = "quarkus.config.source.jdbc.table")
    String tableName;

    @ConfigProperty(name = "quarkus.config.source.jdbc.key")
    String keyColumn;

    @ConfigProperty(name = "quarkus.config.source.jdbc.value")
    String valueColumn;

    @Test
    void correctlyHandleUpdatedValue() {
        given()
                .get("/config/{name}", "greeting.message")
                .then()
                .statusCode(OK.getStatusCode())
                .body("value", equalTo("hello from default table"));

        updateConfig();

        given()
                .get("/config/{name}", "greeting.message")
                .then()
                .statusCode(OK.getStatusCode())
                .body("value", equalTo("updated value"));
    }

    private void updateConfig() {
        var query = "UPDATE " + tableName + " SET " + valueColumn + " = 'updated value' WHERE " + keyColumn
                + " = 'greeting.message'";

        try (var connection = dataSource.getConnection();
                var sql = connection.prepareStatement(query)) {
            var result = sql.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
