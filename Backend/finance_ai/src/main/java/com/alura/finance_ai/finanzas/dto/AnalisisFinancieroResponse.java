package com.alura.finance_ai.finanzas.dto;

import java.math.BigDecimal;
import java.util.Map;

public record AnalisisFinancieroResponse(
        String nombreYApellido,
        String mesYFecha,
        Map<String, BigDecimal> gastosPorCategoria,
        Map<String, BigDecimal> porcentajePorCategoria,
        BigDecimal montoRestante
) {
}
