package com.alura.finance_ai.finanzas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransaccionResponse(
        Long idTransaccion,
        String descripcion,
        BigDecimal valor,
        Long categoriaId,
        String categoriaNombre,
        LocalDate fecha,
        String mensaje
) {
}
