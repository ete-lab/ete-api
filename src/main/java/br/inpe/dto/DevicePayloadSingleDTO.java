package br.inpe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(name = "DeviceSinglePayload", description = "Parâmetros validados com dataword único.")
public record DevicePayloadSingleDTO (
    Integer branch,
    Integer crate,
    Integer station,
    Integer subaddress,
    Integer function,

    @NotNull(message = "O campo dataword não pode ser nulo")
    @Min(value = 0, message = "O valor de dataword deve ser no mínimo 0")
    @Max(value = 1023, message = "O valor de dataword deve ser no máximo 1023")
    @JsonProperty("dataword")
    Integer dataword
) implements  DevicePayloadBase {}

