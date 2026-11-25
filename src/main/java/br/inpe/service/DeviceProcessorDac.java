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
    // canal 2 do DAC  = vertical subida
    // canal 3 do DAC  = vertical patamar
    // canal 4 do DAC  = toroidal subida
    // canal 5 do DAC  = toroidal patamar
    // canal 6 do DAC  = ohmico geração
    // canal 7 do DAC  = ohmico sustentação


    private static final int DATAWORD_COUNT = 8;
    

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @ConfigProperty(name = "device.processor.url", defaultValue = "http://150.163.5.8:8080")
    String processorUrl;

    @ConfigProperty(name = "dac.dataword.maximums", defaultValue = "0,1500,250,8000,250,8000,8000,0")
    List<Integer> datawordMaximums;
    @ConfigProperty(name = "dac.dataword.restrictions", defaultValue = "0,1000,250,4000,250,4000,4000,0")
    List<Integer> datawordRestrictions;

    @jakarta.inject.Inject
    ObjectMapper objectMapper;

    public DevicePayloadDacDTO process(DevicePayloadDacDTO payload) throws IOException, InterruptedException {

        validateDatawords(payload);

        for (int subaddress = 0; subaddress < payload.dataword().size(); subaddress++) {
            DevicePayloadSingleDTO singlePayload = new DevicePayloadSingleDTO(
                    payload.branch(),
                    payload.crate(),
                    payload.station(),
                    subaddress,
                    payload.function(),
                    payload.dataword().get(subaddress)
            );
            ResponseAPIDTO response = send(singlePayload);

            if (response.data() == null) {
                throw new IOException("O serviço remoto não retornou um campo 'data' inteiro válido");
            }
            if (response.qx() == null || response.qx() > 3) {
                throw new IOException("Houve um erro de comunicação com o CAMAC. Código de erro 'qx': " + response.qx());
            }

            payload.dataword().set(subaddress, response.data());
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
        if (datawordRestrictions == null || datawordRestrictions.size() != DATAWORD_COUNT) {
            throw new IllegalStateException(
                    "A configuração dac.dataword.restrictions deve conter exatamente " + DATAWORD_COUNT + " limites."
            );
        }

        int canal = (datawordMaximums == null || datawordMaximums.isEmpty())
                ? -1
                : payload.subaddress();
        if (canal < 0 || canal >= DATAWORD_COUNT) {
            throw new IllegalStateException("Canal valido de 0 - 7. Canal inválido: ." + payload.subaddress());
        }


        for (int i = 0; i < payload.dataword().size(); i++) {
            Integer value = payload.dataword().get(i);
            Integer maximum = datawordMaximums.get(i);
            Integer restriction = datawordRestrictions != null && datawordRestrictions.size() == DATAWORD_COUNT
                    ? datawordRestrictions.get(i)
                    : null;
            
            if (maximum == null || maximum < 0 ) {
                throw new IllegalStateException(
                        "O limite configurado para a fonte no canal " + (i + 1)
                                + " deve ser maior que 0 (zero)."
                );
            }

            if (restriction == null || restriction < 0 ) {
                throw new IllegalStateException(
                        "O limite configurado para a fonte no canal " + (i + 1)
                                + " deve ser maior que 0 (zero)."
                );
            }

            if (value == null) {
                throw new DatawordLimitExceededException("O valor do canal " + (i + 1) + " não pode ser nulo.");
            }
            

            if (value < 0) {
                throw new DatawordLimitExceededException("O dataword do canal " + (i + 1) + " deve ser no mínimo 0 volt.");
            }

            if (value > maximum) {
                throw new DatawordLimitExceededException(
                        "O valor do canal " + (i + 1) + " excede o máximo permitido do fabricante: " + maximum + " volts."
                );
            }

            if (restriction == null || restriction < 0) {
                throw new DatawordLimitExceededException("O limite configurado para o canal " + (i + 1) + " não pode ser nulo e nem negativo.");
            }

            if (restriction != null && value > restriction) {
                throw new DatawordLimitExceededException(
                    "O valor " + value + " do canal " + (i + 1) + " excede o limite configurado pela equipe: " + restriction + " volts."
                );
            }
        }
    }

    private void validateDatawords(DevicePayloadSingleDTO payload) {
        if (payload == null || payload.dataword() == null) {
            throw new DatawordLimitExceededException(
                    "O payload DAC deve conter exatamente 1(um) valor para o canal de saída (dataword)."
            );
        }
        if (datawordMaximums == null || datawordMaximums.size() != DATAWORD_COUNT) {
            throw new IllegalStateException(
                    "A configuração dac.dataword.maximums deve conter exatamente " + DATAWORD_COUNT + " limites."
            );
        }
        if (datawordRestrictions == null || datawordRestrictions.size() != DATAWORD_COUNT) {
            throw new IllegalStateException(
                    "A configuração dac.dataword.restrictions deve conter exatamente " + DATAWORD_COUNT + " limites."
            );
        }
        //para ambos
        int canal = (datawordMaximums.isEmpty())
                ? -1
                : payload.subaddress();

        if (canal < 0 || canal >= DATAWORD_COUNT) {
            throw new IllegalStateException("Canal valido de 0 - 7. Canal inválido: ." + payload.subaddress());
        }

        Integer value = payload.dataword();
        Integer maximum = datawordMaximums.get(payload.subaddress());
        Integer restriction = datawordRestrictions.get(payload.subaddress());

       
        
        if (maximum == null || maximum < 0 ) {
            throw new IllegalStateException(
                    "O limite configurado para a fonte no canal " + (canal + 1)
                            + " deve ser maior que 0 (zero)."
            );
        }

        if (restriction == null || restriction < 0 ) {
            throw new IllegalStateException(
                    "O limite configurado para a fonte no canal " + (canal + 1)
                            + " deve ser maior que 0 (zero)."
            );
        }

        if (value == null) {
            throw new DatawordLimitExceededException("O valor do canal " + (canal + 1) + " não pode ser nulo.");
        }
        

        if (value < 0) {
            throw new DatawordLimitExceededException("O dataword do canal " + (canal + 1) + " deve ser no mínimo 0 volt.");
        }

        if (value > maximum) {
            throw new DatawordLimitExceededException(
                    "O valor do canal " + (canal + 1) + " excede o máximo permitido do fabricante: " + maximum + " volts."
            );
        }

        if (restriction == null || restriction < 0) {
            throw new DatawordLimitExceededException("O limite configurado para o canal " + (canal + 1) + " não pode ser nulo e nem negativo.");
        }

        if (restriction != null && value > restriction) {
            throw new DatawordLimitExceededException(
                "O valor " + value + " do canal " + (canal + 1) + " excede o limite configurado pela equipe: " + restriction + " volts."
            );
        }
        
    }

    private ResponseAPIDTO send(DevicePayloadSingleDTO payload) throws IOException, InterruptedException {
        String requestBody;
        int valorRegistrador = 
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

        validateDatawords(payload);

        ResponseAPIDTO response = send(payload);
        
        if (response.data() == null) {
                throw new IOException("O serviço remoto não retornou um campo 'data' inteiro válido");
        }

        if (response.qx() == null || response.qx() > 3) {
                throw new IOException("Houve um erro de comunicação com o CAMAC. Código de erro 'qx': " + response.qx());
        }

        DevicePayloadSingleDTO updatedPayload = new DevicePayloadSingleDTO(
            payload.branch(),
            payload.crate(),
            payload.station(),
            payload.subaddress(),
            payload.function(),
            response.data()
        );
        return send(updatedPayload);

    }

    public static class DatawordLimitExceededException extends IllegalArgumentException {
        public DatawordLimitExceededException(String message) {
            super(message);
        }
    }
}