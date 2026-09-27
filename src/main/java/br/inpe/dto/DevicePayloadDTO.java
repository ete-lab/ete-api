package br.inpe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DevicePayloadDTO(
    @NotNull(message = "O campo branch não pode ser nulo")
    @Min(value = 0, message = "O valor mínimo para branch é 0")
    @Max(value = 2, message = "O valor máximo para branch é 2")
    @JsonProperty("branch")
    Integer branch,

    @NotNull(message = "O campo crate não pode ser nulo")
    @Min(value = 0, message = "O valor mínimo para crate é 0")
    @Max(value = 2, message = "O valor máximo para crate é 2")
    @JsonProperty("crate")
    Integer crate,

    @NotNull(message = "O campo station não pode ser nulo")
    @Min(value = 1, message = "O valor mínimo para station é 1")
    @Max(value = 24, message = "O valor máximo para station é 24")
    @JsonProperty("station")
    Integer station,

    @NotNull(message = "O campo subaddress não pode ser nulo")
    @Min(value = 0, message = "O valor mínimo para subaddress é 0")
    @Max(value = 1, message = "O valor máximo para subaddress é 1")
    @JsonProperty("subaddress")
    Integer subaddress,
    
    @NotNull(message = "O campo function não pode ser nulo")
    @Min(value = 1, message = "O valor mínimo para function é 1")
    @Max(value = 30, message = "O valor máximo para function é 30")
    @JsonProperty("function")
    Integer function,

    @Min(value = 0, message = "O valor mínimo para dataword é 0")
    @Max(value = 65535, message = "O valor máximo para dataword é 65535")
    @NotNull(message = "O campo dataword não pode ser nulo")
    @JsonProperty("dataword")
    Long dataword
) {}
