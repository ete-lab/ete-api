package br.inpe.resource;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

@QuarkusTest
@QuarkusTestResource(value = DacWireMockTestResource.class, restrictToAnnotatedClass = true)
class DacResourceTest {
    private static final String REQUEST_JSON = """
            {"branch":0,"crate":0,"station":9,"subaddress":0,"function":1,
             "dataword":[10,20,30,40,50,60,70,80]}""";

    @BeforeEach
    void resetWireMock() {
        DacWireMockTestResource.server().resetAll();
    }

    @Test
    @DisplayName("POST processa os oito datawords e agrega a resposta")
    void shouldProcessDatawords() {
        stubRemoteResponse(1978, 1);

        given().contentType(ContentType.JSON).body(REQUEST_JSON)
            .when().post("/dac")
            .then().statusCode(201)
            .body("status", equalTo("Sucesso"))
            .body("comando", equalTo("read"))
            .body("mensagem", equalTo("Sucesso na conexao"))
            .body("data", hasItems(1978))
            .body("qx", equalTo(1));

        DacWireMockTestResource.server().verify(8,
            postRequestedFor(urlEqualTo("/dac")).withHeader("Content-Type", containing("application/json")));
    }

    @Test
    @DisplayName("GET usa o payload padrão do DAC")
    void shouldProcessDefaultPayloadWithGet() {
        stubRemoteResponse(42, 0);

        given().when().get("/dac")
            .then().statusCode(200)
            .body("status", equalTo("Sucesso"))
            .body("data", hasItems(42))
            .body("qx", equalTo(0));

        DacWireMockTestResource.server().verify(8, postRequestedFor(urlEqualTo("/dac")));
    }

    @Test
    @DisplayName("Payload com menos de oito datawords é rejeitado")
    void shouldRejectInvalidDatawordCount() {
        given().contentType(ContentType.JSON)
            .body("{\"branch\":0,\"crate\":0,\"station\":9,\"subaddress\":0,\"function\":1,\"dataword\":[10,20]}")
            .when().post("/dac")
            .then().statusCode(400);

        DacWireMockTestResource.server().verify(0, postRequestedFor(urlEqualTo("/dac")));
    }

    @Test
    @DisplayName("Falha remota retorna 502")
    void shouldReturnBadGateway() {
        DacWireMockTestResource.server().stubFor(post(urlEqualTo("/dac"))
            .willReturn(aResponse().withStatus(500)));

        given().contentType(ContentType.JSON).body(REQUEST_JSON)
            .when().post("/dac")
            .then().statusCode(502)
            .body("error", equalTo("Falha ao processar dados no dispositivo remoto"));
    }

    private void stubRemoteResponse(int data, int qx) {
        DacWireMockTestResource.server().stubFor(post(urlEqualTo("/dac"))
            .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                .withBody("{\"status\":\"Sucesso\",\"comando\":\"read\",\"mensagem\":\"Sucesso na conexao\",\"data\":" + data + ",\"qx\":" + qx + "}")));
    }
}
