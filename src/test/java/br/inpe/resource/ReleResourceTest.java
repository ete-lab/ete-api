package br.inpe.resource;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@QuarkusTestResource(value = ReleWireMockTestResource.class, restrictToAnnotatedClass = true)
class ReleResourceTest {

    private static final String REQUEST_JSON = """
            {
                "branch": 0,
                "crate": 0,
                "station": 9,
                "subaddress": 0,
                "function": 1,
                "dataword": 1987
            }
            """;

    @BeforeEach
    void resetWireMock() {
        ReleWireMockTestResource.server().resetAll();
    }

    @Test
    @DisplayName("Encaminha o comando read e retorna a resposta de sucesso do serviço remoto")
    void shouldForwardReadCommandAndReturnSuccessResponse() {
        ReleWireMockTestResource.server().stubFor(post(urlEqualTo("/rele"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "status": "Sucesso",
                                    "comando": "read",
                                    "mensagem": "Sucesso na conexao",
                                    "data": 1978,
                                    "qx": 1
                                }
                                """)));

        given()
                .contentType(ContentType.JSON)
                .body(REQUEST_JSON)
                .when()
                .post("/rele")
                .then()
                .statusCode(201)
                .body("status", equalTo("Sucesso"))
                .body("comando", equalTo("read"))
                .body("mensagem", equalTo("Sucesso na conexao"))
                .body("data", equalTo(1978))
                .body("qx", equalTo(1));

        verifyExternalRequest();
    }

    @Test
    @DisplayName("Retorna o erro informado pelo serviço remoto")
    void shouldReturnErrorResponseFromExternalService() {
        ReleWireMockTestResource.server().stubFor(post(urlEqualTo("/rele"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "status": "erro",
                                    "comando": "execute",
                                    "mensagem": "JSON invalido.",
                                    "data": 0,
                                    "qx": 1
                                }
                                """)));

        given()
                .contentType(ContentType.JSON)
                .body(REQUEST_JSON)
                .when()
                .post("/rele")
                .then()
                .statusCode(201)
                .body("status", equalTo("erro"))
                .body("comando", equalTo("execute"))
                .body("mensagem", equalTo("JSON invalido."))
                .body("data", equalTo(0))
                .body("qx", equalTo(1));

        verifyExternalRequest();
    }

    @Test
    @DisplayName("Aceita também uma resposta do comando write")
    void shouldReturnWriteCommandResponse() {
        ReleWireMockTestResource.server().stubFor(post(urlEqualTo("/rele"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "status": "Sucesso",
                                    "comando": "write",
                                    "mensagem": "Sucesso na conexao",
                                    "data": 1978,
                                    "qx": 1
                                }
                                """)));

        given()
                .contentType(ContentType.JSON)
                .body(REQUEST_JSON)
                .when()
                .post("/rele")
                .then()
                .statusCode(201)
                .body("comando", equalTo("write"));

        verifyExternalRequest();
    }

    private void verifyExternalRequest() {
        ReleWireMockTestResource.server().verify(1, postRequestedFor(urlEqualTo("/rele"))
            .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(equalToJson(REQUEST_JSON)));
    }
}
