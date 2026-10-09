package br.inpe;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
class OpenApiResourceTest {

    @Test
    void openApiDocumentDescribesDeviceOperations() {
        given()
                .when()
                .get("/q/openapi?format=json")
                .then()
                .statusCode(200)
                .body("info.title", equalTo("ETE API"))
                .body("info.version", equalTo("1.0.0"))
                .body("paths", hasKey("/rele"))
                .body("paths['/rele'].get", hasKey("responses"))
                .body("paths['/rele'].post", hasKey("requestBody"));
    }

    @Test
    void swaggerUiIsAvailable() {
        given()
                .when()
                .get("/q/swagger-ui/")
                .then()
                .statusCode(200)
                .body(containsString("swagger-ui"));
    }
}
