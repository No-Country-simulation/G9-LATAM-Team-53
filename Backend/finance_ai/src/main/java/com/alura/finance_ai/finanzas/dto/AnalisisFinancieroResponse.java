package com.alura.finance_ai.finanzas.dto;

import java.util.List;
import java.util.Map;

public record AnalisisFinancieroResponse(
        String perfilFinanciero,
        Double probabilidad,
        Map<String, Double> resumenGastos,
        List<String> recomendaciones
) {
}
