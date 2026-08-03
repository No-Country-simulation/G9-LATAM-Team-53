package com.alura.finance_ai.finanzas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransaccionResponse(
        Long id,
        String descripcion,
        BigDecimal valor,
        String categoria,
        LocalDate fecha,
        String mensaje
) {}