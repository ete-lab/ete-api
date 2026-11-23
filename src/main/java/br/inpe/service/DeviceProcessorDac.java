package br.inpe.service;

import br.inpe.dto.DevicePayloadDacDTO;
import br.inpe.dto.DevicePayloadSingleDTO;
import br.inpe.dto.ResponseAPIDTO;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.constraints.Null;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@ApplicationScoped
public class DeviceProcessorDac {

    private static final int DATAWORD_COUNT = 8;
    

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @ConfigProperty(name = "device.processor.url")
    String processorUrl;

    @ConfigProperty(name = "dac.dataword.maximums", defaultValue = "0,250,4000,200,3000,2000,5000,0")
    List<Integer> datawordMaximums;

    @jakarta.inject.Inject
    ObjectMapper objectMapper;

    public DevicePayloadDacDTO process(DevicePayloadDacDTO payload) throws IOException, InterruptedException {

        validateDatawords(payload);

        for (int i = 0; i < payload.dataword().size(); i++) {
            DevicePayloadSingleDTO singlePayload = new DevicePayloadSingleDTO(
                    payload.branch(),
                    payload.crate(),
                    payload.station(),
                    payload.subaddress(),
                    payload.function(),
                    payload.dataword().get(i)
            );
            ResponseAPIDTO response = send(singlePayload);

            if (response.data() == null) {
                throw new IOException("O serviço remoto não retornou um campo 'data' inteiro válido");
            }
            if (response.qx() == null || response.qx() > 3) {
                throw new IOException("Houve um erro de comunicação com o CAMAC. Código de erro 'qx': " + response.qx());
            }

            payload.dataword().set(i, response.data());
        }

        return payload;
    }

    private void validateDatawords(DevicePayloadDacDTO payload) {
        if (payload == null || payload.dataword() == null || payload.dataword().size() != DATAWORD_COUNT) {
            throw new DatawordLimitExceededException(
                    "O payload DAC deve conter exatamente " + DATAWORD_COUNT + " valores dos canais de saída (dataword)."
            );
        }
        if (datawordMaximums == null || datawordMaximums.size() != DATAWORD_COUNT) {
            throw new IllegalStateException(
                    "A configuração dac.dataword.maximums deve conter exatamente " + DATAWORD_COUNT + " limites."
            );
        }

        for (int i = 0; i < payload.dataword().size(); i++) {
            Integer value = payload.dataword().get(i);
            Integer maximum = datawordMaximums.get(i);

            if (value == null) {
                throw new DatawordLimitExceededException("O dataword da posição " + i + " não pode ser nulo.");
            }
            if (maximum == null || maximum < 0 ) {
                throw new IllegalStateException(
                        "O limite configurado para o dataword da posição " + i
                                + " deve ser maior que 0 (zero)."
                );
            }
            if (value < 0) {
                throw new DatawordLimitExceededException("O dataword da posição " + i + " deve ser no mínimo 0 volt.");
            }
            if (value > maximum) {
                throw new DatawordLimitExceededException(
                        "O dataword da posição " + i + " excede o máximo permitido de " + maximum + " volts."
                );
            }
        }
    }

    private ResponseAPIDTO send(DevicePayloadSingleDTO payload) throws IOException, InterruptedException {
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
    
    public ResponseAPIDTO process(DevicePayloadSingleDTO payload) throws IOException, InterruptedException {
        if (payload == null || payload.dataword() == null) {
            throw new DatawordLimitExceededException("O payload e o dataword não podem ser nulos.");
        }
        
        int maximum = (datawordMaximums == null || datawordMaximums.isEmpty())
                ? -1
                : datawordMaximums.get(payload.subaddress() != null && payload.subaddress() >= 0 && payload.subaddress() < datawordMaximums.size()
                    ? payload.subaddress()
                    : 0);

        if (maximum < 0 ) {
            throw new IllegalStateException("O limite configurado deve ser maior que 0 .");
        }
        if (payload.dataword() < 0) {
            throw new DatawordLimitExceededException("O dataword deve ser no mínimo 0 volt.");
        }
        if (payload.dataword() > maximum) {
            throw new DatawordLimitExceededException("O dataword excede o máximo permitido de " + maximum + ".");
        }

        return send(payload);
    }

    public static class DatawordLimitExceededException extends IllegalArgumentException {
        public DatawordLimitExceededException(String message) {
            super(message);
        }
    }
}