package br.inpe.resource;

import java.io.IOException;
import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import jakarta.ws.rs.Produces;
import br.inpe.dto.DevicePayloadDacDTO;
import br.inpe.dto.ResponseDacDTO;
import br.inpe.service.DeviceProcessorDac;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/adc")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "ADC", description = "Operações de leitura e processamento de dados do dispositivo ADC  .")
public class adc {

        private static final DevicePayloadDacDTO REGISTRO_PAYLOAD =
            new DevicePayloadDacDTO(0, 0, 9, 0, 1, new Integer[] {0, 0, 0, 0, 0, 0, 0, 0});
    //private static int register = 0;

    @Inject
    DeviceProcessorDac deviceProcessor;

    
    @GET
    @Operation(summary = "Ler dados do ADC", description = "Envia o comando padrão ao dispositivo e retorna os dados recebidos.")
    @APIResponse(responseCode = "200", description = "Comando processado com sucesso.", content = @Content(schema = @Schema(implementation = ResponseDacDTO.class)))
    @APIResponse(responseCode = "502", description = "Falha ao processar os dados no dispositivo remoto.")
    public Response registro() {
        try {
            ResponseDacDTO result = deviceProcessor.process(REGISTRO_PAYLOAD);
            return Response.status(Response.Status.OK)
                    .entity(result)
                    .build();
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_GATEWAY)
                            .entity(Map.of("error", "Falha ao processar dados no dispositivo remoto"))
                            .build());
        }
    }

    @POST
    @Operation(summary = "Processar comando do dispositivo", description = "Valida e encaminha os parâmetros do comando ao dispositivo remoto.")
    @APIResponse(responseCode = "201", description = "Comando processado com sucesso.", content = @Content(schema = @Schema(implementation = ResponseDacDTO.class)))
    @APIResponse(responseCode = "400", description = "Payload inválido ou com erro de desserialização.")
    @APIResponse(responseCode = "502", description = "Falha ao processar os dados no dispositivo remoto.")
    public Response processData(@Valid DevicePayloadDacDTO payload) {
        try {
            ResponseDacDTO result = deviceProcessor.process(payload);
            return Response.status(Response.Status.CREATED)
                    .entity(result)
                    .build();
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return Response.status(Response.Status.BAD_GATEWAY)
                    .entity(Map.of("error", "Falha ao processar dados no dispositivo remoto"))
                    .build();
        }
    }
}

