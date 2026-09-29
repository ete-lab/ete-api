package br.inpe.resource;

import br.inpe.dto.DevicePayloadDTO;
import br.inpe.dto.ResponseAPIDTO;
import br.inpe.service.DeviceProcessor;
import java.io.IOException;
import java.util.Map;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/relay")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeviceResource {

    private static final DevicePayloadDTO REGISTRO_PAYLOAD =
            new DevicePayloadDTO(0, 0, 9, 0, 0, 0);
    //private static int register = 0;

    @Inject
    DeviceProcessor deviceProcessor;

    
    @GET
    public Response registro() {
        try {
            ResponseAPIDTO result =deviceProcessor.process(REGISTRO_PAYLOAD);
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
    public Response processData(@Valid DevicePayloadDTO payload) {
        try {
            ResponseAPIDTO result = deviceProcessor.process(payload);
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
