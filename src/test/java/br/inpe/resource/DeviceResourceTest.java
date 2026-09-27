package br.inpe.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@QuarkusTestResource(LocalDeviceProcessor.class)
public class DeviceResourceTest {

    @Test
    @DisplayName("Deve encaminhar o payload e retornar o inteiro recebido do serviço remoto")
    public void testProcessDataSuccess() {
        String jsonPayload = """
                {
                    "branch": 0,
                    "crate": 0,
                    "station": 9,
                    "subaddress": 0,
                    "function": 25,
                    "dataword": 198554
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(jsonPayload)
                .when()
                .post("/api/device")
                .then()
                .statusCode(201)
                .body(equalTo(42));

        assertTrue(
                LocalDeviceProcessor.receivedBody.contains("\"branch\":0"));
        assertTrue(
                LocalDeviceProcessor.receivedBody.contains("\"dataword\":198554"));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao enviar payload inválido")
    public void testProcessDataInvalid() {
        String invalidPayload = """
                {
                    "branch": -1,
                    "dataword": 198554
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(invalidPayload)
                .when()
                .post("/api/device")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Deve retornar JSON customizado com erros de validação ao enviar dados inválidos")
    public void testCustomValidationErrorResponse() {
        String invalidJson = """
                {
                    "branch": -1
                }
                """;
        given()
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .post("/api/device")
                .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("error", equalTo("Erro de validação nos campos informados"))
                .body("errors.find { it.field == 'branch' }.message", equalTo("O valor mínimo para branch é 0"));
    }

    @Test
    @DisplayName("Deve retornar 400 ao enviar tipo de dado incompatível no JSON")
    public void testInvalidDataType() {
        String jsonWithWrongType = """
                {
                    "branch": 0,
                    "crate": 0,
                    "station": "nove_em_texto",
                    "subaddress": 0,
                    "function": 25,
                    "dataword": 198554
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(jsonWithWrongType)
                .when()
                .post("/api/device")
                .then()
                .statusCode(400)
                .body("error", equalTo("Erro na desserialização do JSON"))
                .body("field", equalTo("station"));
    }

    @Test
    @DisplayName("Deve retornar 400 ao enviar JSON com sintaxe malformada")
    public void testMalformedJsonSyntax() {
        String malformedJson = "{ \"branch\": 0, }"; // Vírgula no final sem próximo campo

        given()
                .contentType(ContentType.JSON)
                .body(malformedJson)
                .when()
                .post("/api/device")
                .then()
                .statusCode(400)
                .body("error", equalTo("Erro na desserialização do JSON"))
                .body("message", equalTo("Sintaxe do JSON malformada (verifique vírgulas, aspas ou chaves)."));
    }

}
