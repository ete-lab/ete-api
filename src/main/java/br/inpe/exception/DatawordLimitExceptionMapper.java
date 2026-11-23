package br.inpe.exception;

import br.inpe.service.DeviceProcessorDac.DatawordLimitExceededException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.Instant;

@Provider
public class DatawordLimitExceptionMapper implements ExceptionMapper<DatawordLimitExceededException> {

    @Override
    public Response toResponse(DatawordLimitExceededException exception) {
        ValidationErrorResponse responseBody = new ValidationErrorResponse(
                Instant.now(),
                Response.Status.BAD_REQUEST.getStatusCode(),
                "Erro de validação nos datawords",
                java.util.List.of(new ValidationErrorResponse.FieldError("dataword", exception.getMessage()))
        );

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(responseBody)
                .build();
    }
}
