package br.inpe.exception;
import java.time.Instant;
import java.util.List;

public record ValidationErrorResponse(
    Instant timestamp,
    int status,
    String error,
    List<FieldError> errors
) {
    public record FieldError(
        String field,
        String message
    ) {}
}
