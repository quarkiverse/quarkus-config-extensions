package io.quarkus.it.config.jasypt;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SecretResourceTest {
    @Test
    void secret() {
        given()
                .when().get("/secret")
                .then()
                .statusCode(200)
                .body(is("12345678"));
    }
}
