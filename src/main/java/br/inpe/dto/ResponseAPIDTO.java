package br.inpe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ResponseAPIDTO(
    @NotNull(message = "O campo data não pode ser nulo")
    @JsonProperty("status") String status,

    @NotNull(message = "O campo data não pode ser nulo")
    @JsonProperty("comando") String comando,

    @NotNull(message = "O campo data não pode ser nulo")
    @JsonProperty("mensagem") String mensagem,

    @Min(value = 0, message = "O valor mínimo para data é 0")
    @Max(value = 65535, message = "O valor máximo para data é 65535")
    @NotNull(message = "O campo data não pode ser nulo")
    @JsonProperty("data") Integer data,

    @NotNull(message = "O campo qx não pode ser nulo")
    @Min(value = 0, message = "O valor mínimo para qx é 0")
    @Max(value = 65535, message = "O valor máximo para qx é 99") 
    @JsonProperty("qx") Integer qx
) {}