package com.alura.finance_ai.finanzas.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record IngresoMensualRequest(
        @NotNull(message = "El ingreso mensual es obligatorio")
        @Positive(message = "El ingreso mensual debe ser mayor a cero")
        BigDecimal ingresoMensual
) {
}
