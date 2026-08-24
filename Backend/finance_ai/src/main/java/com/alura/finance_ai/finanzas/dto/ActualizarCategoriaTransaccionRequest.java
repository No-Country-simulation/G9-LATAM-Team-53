package com.alura.finance_ai.finanzas.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ActualizarCategoriaTransaccionRequest(
        @NotNull(message = "La categoria es obligatoria")
        @Positive(message = "La categoria debe ser valida")
        Long categoriaId
) {
}
