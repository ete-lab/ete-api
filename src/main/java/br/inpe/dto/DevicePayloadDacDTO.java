package br.inpe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "DeviceAdcPayload", description = "Parâmetros validados para envio de um comando ao dispositivo ADC.")
public record DevicePayloadDacDTO(
    Integer branch,
    Integer crate,
    Integer station,
    Integer subaddress,
    Integer function,
    
    @NotNull(message = "O campo dataword não pode ser nulo")
    @Size(min = 8, max = 8, message = "O campo dataword deve conter exatamente 8 inteiros")
    @JsonProperty("dataword")
    List<
        @Min(value = 0, message = "Cada valor de dataword deve ser no mínimo 0")
        @Max(value = 1023, message = "Cada valor de dataword deve ser no máximo 1023")
    Integer> dataword

) implements DevicePayloadBase {}