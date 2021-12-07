package io.quarkiverse.config.sample.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ConfigExtensionsResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/config-extensions")
                .then()
                .statusCode(200)
                .body(is("Hello config-extensions"));
    }
}
