package io.quarkiverse.jdbi.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class JdbiResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/jdbi")
                .then()
                .statusCode(200)
                .body(is("OK"));
    }
}
