package br.inpe;

import jakarta.ws.rs.core.MediaType;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class GreetingResourceTest {
    // "UP" is the current response contract implemented by GreetingResource.
    private static final String GREETING = "UP";

    @Test
    void testHelloReturnsGreeting() {
        assertEquals(GREETING, new GreetingResource().hello());
    }

    @Test
    void testHelloEndpoint() {
        given()
          .when().get("/hello")
          .then()
             .statusCode(200)
             .contentType(MediaType.TEXT_PLAIN)
             .body(is(GREETING));
    }

    @Test
    void testHealfEndpoint() {
        given()
          .when().get("/hello/healf")
          .then()
             .statusCode(200)
             .contentType(MediaType.TEXT_PLAIN)
             .body(is("Testing"));
    }

}