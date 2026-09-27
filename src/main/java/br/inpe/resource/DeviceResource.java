package br.inpe.resource;

import br.inpe.dto.DevicePayloadDTO;
import br.inpe.service.DeviceProcessor;
import java.io.IOException;
import java.util.Map;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/device")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeviceResource {

    @Inject
    DeviceProcessor deviceProcessor;

    @POST
    public Response processData(@Valid DevicePayloadDTO payload) {
        try {
            int result = deviceProcessor.process(payload);
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
