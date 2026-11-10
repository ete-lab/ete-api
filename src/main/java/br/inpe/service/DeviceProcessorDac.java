package br.inpe.service;

import br.inpe.dto.DevicePayloadDTO;
import br.inpe.dto.DevicePayloadDacDTO;
import br.inpe.dto.ResponseAPIDTO;
import br.inpe.dto.ResponseDacDTO;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@ApplicationScoped
public class DeviceProcessorDac {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @ConfigProperty(name = "device.processor.url")
    String processorUrl;

    @jakarta.inject.Inject
    ObjectMapper objectMapper;

    public ResponseDacDTO process(DevicePayloadDacDTO payload) throws IOException, InterruptedException {
        if (payload == null || payload.dataword() == null || payload.dataword().length != 8) {
            throw new IOException("O payload DAC deve conter exatamente 8 valores de dataword");
        }

        Integer[] data = new Integer[8];
        ResponseAPIDTO lastResponse = null;

        for (int i = 0; i < payload.dataword().length; i++) {
            DevicePayloadDTO singlePayload = new DevicePayloadDTO(
                    payload.branch(),
                    payload.crate(),
                    payload.station(),
                    payload.subaddress(),
                    payload.function(),
                    payload.dataword()[i]);
            ResponseAPIDTO response = send(singlePayload);

            if (response.data() == null) {
                throw new IOException("O serviço remoto não retornou um campo 'data' inteiro válido");
            }
            if (response.qx() == null || response.qx() > 3) {
                throw new IOException("Houve um erro de comunicação com o CAMAC. Código de erro 'qx': " + response.qx());
            }

            data[i] = response.data();
            lastResponse = response;
        }

        if (lastResponse == null) {
            throw new IOException("O serviço remoto não retornou respostas para os datawords");
        }

        return new ResponseDacDTO(
                lastResponse.status(),
                lastResponse.comando(),
                lastResponse.mensagem(),
                data,
                lastResponse.qx());
    }

    private ResponseAPIDTO send(DevicePayloadDTO payload) throws IOException, InterruptedException {
        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IOException("Erro ao serializar o payload do dispositivo", exception);
        }

        HttpRequest request = HttpRequest.newBuilder(URI.create(processorUrl))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("O serviço remoto retornou HTTP " + response.statusCode());
        }

        try {
            return objectMapper.readValue(response.body(), ResponseAPIDTO.class);
        } catch (JsonProcessingException exception) {
            throw new IOException("Erro ao processar a resposta JSON do serviço remoto", exception);
        }
    }
}