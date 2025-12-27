package io.quarkus.it.config.nacos;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class NacosConfigTest {
    @Test
    void config() {
        given()
                .get("/config/{name}", "foo.bar")
                .then()
                .statusCode(OK.getStatusCode())
                .body("value", equalTo("1234"));
    }
}
