package br.inpe.exception;
    
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Provider
public class JsonSerializationExceptionMapper implements ExceptionMapper<com.fasterxml.jackson.core.JsonProcessingException> {

    @Override
    public Response toResponse(com.fasterxml.jackson.core.JsonProcessingException exception) {
        String detailMessage = "Formato do JSON inválido";
        String fieldName = null;

        if (exception instanceof InvalidFormatException ife) {
            fieldName = extractFieldName(ife.getPath());
            String targetType = ife.getTargetType() != null ? ife.getTargetType().getSimpleName() : "desconhecido";
            detailMessage = String.format("O valor '%s' é inválido para o campo '%s'. Esperado um tipo %s.",
                    ife.getValue(), fieldName, targetType);

        } else if (exception instanceof MismatchedInputException mie) {
            fieldName = extractFieldName(mie.getPath());
            detailMessage = fieldName != null 
                    ? String.format("Tipo de dado incompatível para o campo '%s'.", fieldName)
                    : "Estrutura do JSON incompatível com o objeto esperado.";

        } else if (exception instanceof JsonParseException) {
            detailMessage = "Sintaxe do JSON malformada (verifique vírgulas, aspas ou chaves).";
        }

        JsonErrorResponse responseBody = new JsonErrorResponse(
                Instant.now(),
                Response.Status.BAD_REQUEST.getStatusCode(),
                "Erro na desserialização do JSON",
                detailMessage,
                fieldName
        );

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(responseBody)
                .build();
    }

    private String extractFieldName(List<JsonMappingException.Reference> path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        return path.stream()
                .map(JsonMappingException.Reference::getFieldName)
                .filter(name -> name != null)
                .collect(Collectors.joining("."));
    }

    public record JsonErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String field
    ) {}
}