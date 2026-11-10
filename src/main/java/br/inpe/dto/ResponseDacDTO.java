package br.inpe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "DeviceAdcResponse", description = "Resultado retornado após o processamento do comando pelo dispositivo ADC.")
public record ResponseDacDTO(
    @NotNull(message = "O campo status não pode ser nulo")
    @JsonProperty("status") String status,

    @NotNull(message = "O campo comando não pode ser nulo")
    @JsonProperty("comando") String comando,

    @NotNull(message = "O campo mensagem não pode ser nulo")
    @JsonProperty("mensagem") String mensagem,

    @NotNull(message = "O campo data não pode ser nulo")
    @Size(min = 8, max = 8, message = "O campo data deve conter exatamente 8 inteiros")
    @JsonProperty("data")
    java.lang.@Min(value = 0, message = "Cada valor de data deve ser no mínimo 0")
    @Max(value = 65535, message = "Cada valor de data deve ser no máximo 65535") Integer[] data,

    @NotNull(message = "O campo qx não pode ser nulo")
    @Min(value = 0, message = "O valor mínimo para qx é 0")
    @Max(value = 65535, message = "O valor máximo para qx é 65535")
    @JsonProperty("qx") Integer qx
) {}