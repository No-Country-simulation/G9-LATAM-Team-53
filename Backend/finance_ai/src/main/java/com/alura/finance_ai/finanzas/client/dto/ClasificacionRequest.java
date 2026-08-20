package com.alura.finance_ai.finanzas.client.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ClasificacionRequest(
        String descripcion,
        BigDecimal valor,
        LocalDate fecha
) {
}
