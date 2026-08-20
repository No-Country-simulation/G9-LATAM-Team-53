package com.alura.finance_ai.finanzas.dto;

import java.math.BigDecimal;

public record IngresoMensualResponse(
        Long userId,
        BigDecimal ingresoMensual,
        String mensaje
) {
}
