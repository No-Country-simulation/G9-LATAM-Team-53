package com.alura.finance_ai.finanzas.dto;

import com.alura.finance_ai.finanzas.model.PerfilFinanciero;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record AnalisisFinancieroResponse(
        String nombreYApellido,
        String mesYFecha,
        Map<String, BigDecimal> gastosPorCategoria,
        Map<String, BigDecimal> porcentajePorCategoria,
        BigDecimal montoRestante,
        PerfilFinanciero perfilFinanciero,
        List<String> recomendaciones
) {
}
