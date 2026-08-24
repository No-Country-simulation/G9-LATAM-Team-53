package com.alura.finance_ai.finanzas.dto;

import java.math.BigDecimal;

public record EstadoIngresoMensualResponse(
        boolean registrado,
        BigDecimal ingresoMensual
) {
}