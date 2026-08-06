package com.alura.finance_ai.finanzas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransaccionRequest(
        @NotBlank(message = "La descripcion no puede estar vacia")
        String descripcion,

        @NotNull(message = "El valor es obligatorio")
        @Positive(message = "El valor debe ser mayor a cero")
        BigDecimal valor,

        @NotNull(message = "La categoria es obligatoria")
        @Positive(message = "La categoria debe ser valida")
        Long categoriaId,

        @NotNull(message = "La fecha es obligatoria")
        LocalDate fecha
) {
}
