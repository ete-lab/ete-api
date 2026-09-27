package br.inpe.service;

import br.inpe.dto.DevicePayloadDTO;
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
public class DeviceProcessor {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @ConfigProperty(name = "device.processor.url", defaultValue = "http://150.163.5.83:8080/")
    String processorUrl;

    @jakarta.inject.Inject
    ObjectMapper objectMapper;

    public int process(DevicePayloadDTO payload) throws IOException, InterruptedException {
        String requestBody = objectMapper.writeValueAsString(payload);
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
            return Integer.parseInt(response.body().trim());
        } catch (NumberFormatException exception) {
            throw new IOException("O serviço remoto não retornou um inteiro válido", exception);
        }
    }
}