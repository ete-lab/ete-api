package br.inpe.service;

import br.inpe.dto.DevicePayloadDTO;
import br.inpe.dto.DevicePayloadDacDTO;
import br.inpe.dto.ResponseAPIDTO;
import br.inpe.dto.ResponseDacDTO;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonMappingException;
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

    ResponseDacDTO responseBody;

    public ResponseDacDTO process(DevicePayloadDacDTO payload) throws IOException, InterruptedException {
        
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
            responseBody = objectMapper.readValue(response.body(), ResponseDacDTO.class);
        } catch (JsonMappingException e) {
            throw new IOException("Erro ao mapear o JSON para ResponseAPIDTO: " + e.getMessage());
        } catch (StreamReadException e) {
            throw new IOException("Erro ao ler o fluxo de dados JSON: " + e.getMessage());
        } catch (DatabindException e) {
            throw new IOException("Erro ao vincular os dados JSON: " + e.getMessage());
        } catch (JsonProcessingException e) {
            throw new IOException("Erro ao processar o JSON: " + e.getMessage());
        } catch (Exception e) {
            throw new IOException("Erro inesperado ao processar a resposta JSON: " + e.getMessage());
        }
        
        if (responseBody.data() == null) {
            throw new IOException("O serviço remoto não retornou um campo 'data' inteiro válido");
        }
        if(responseBody.qx() > 3) {
            throw new IOException("Houve um erro de comunicação com o CAMAC. Código de erro 'qx': " + responseBody.qx());
        }
        return responseBody;
    }
}