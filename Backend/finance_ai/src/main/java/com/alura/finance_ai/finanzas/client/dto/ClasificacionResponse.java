package com.alura.finance_ai.finanzas.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ClasificacionResponse(
        @JsonProperty("categoria_predicha") String categoriaPredicha,
        double confianza
) {
}
