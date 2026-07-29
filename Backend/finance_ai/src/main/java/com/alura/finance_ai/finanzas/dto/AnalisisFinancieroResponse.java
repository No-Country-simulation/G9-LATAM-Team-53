package com.alura.finance_ai.finanzas.dto;

import java.util.List;
import java.util.Map;

public record AnalisisFinancieroResponse(
        String perfil_financiero,
        Double probabilidad,
        Map<String, Double> resumen_gastos,
        List<String> recomendaciones
) {}

