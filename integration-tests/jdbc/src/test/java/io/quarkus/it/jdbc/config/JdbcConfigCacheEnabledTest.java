package io.quarkus.it.jdbc.config;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(CacheEnabledProfile.class)
@QuarkusTestResource(PostgresqlTestResource.class)
public class JdbcConfigCacheEnabledTest {
    @Test
    void jdbcConfig() {
        given()
                .get("/config/{name}", "greeting.message")
                .then()
                .statusCode(OK.getStatusCode())
                .body("value", equalTo("hello from default table"));
    }
}
