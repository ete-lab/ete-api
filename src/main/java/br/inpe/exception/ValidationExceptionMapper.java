package br.inpe.exception;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.Instant;
import java.util.List;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        List<ValidationErrorResponse.FieldError> fieldErrors = exception.getConstraintViolations()
                .stream()
                .map(this::mapViolationToFieldError)
                .toList();

        ValidationErrorResponse responseBody = new ValidationErrorResponse(
                Instant.now(),
                Response.Status.BAD_REQUEST.getStatusCode(),
                "Erro de validação nos campos informados",
                fieldErrors
        );

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(responseBody)
                .build();
    }

    private ValidationErrorResponse.FieldError mapViolationToFieldError(ConstraintViolation<?> violation) {
        String fullPath = violation.getPropertyPath().toString();
        // Extrai apenas o nome do campo do caminho completo (ex: "processData.payload.branch" -> "branch")
        String fieldName = fullPath.substring(fullPath.lastIndexOf('.') + 1);

        return new ValidationErrorResponse.FieldError(fieldName, violation.getMessage());
    }
}
