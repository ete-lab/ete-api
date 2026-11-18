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
          .when().get("/up")
          .then()
             .statusCode(200)
             .contentType(MediaType.TEXT_PLAIN)
             .body(is(GREETING));
    }

    @Test
    void testHealthEndpoint() {
        given()
          .when().get("/up/health")
          .then()
             .statusCode(200)
             .contentType(MediaType.TEXT_PLAIN)
             .body(is("Socket url em DEVICE_PROCESSOR_URL\": http://host.docker.internal:8080\n" + //
                                  "Socket url em prod: null\n" + //
                                  "Socket url: em devnull"));
    }

}