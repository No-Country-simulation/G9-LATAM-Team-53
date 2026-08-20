package com.alura.finance_ai.finanzas.model;

import lombok.Getter;

@Getter
public enum PerfilFinanciero {
    EN_OBSERVACION("En observación"),
    SALUDABLE("Saludable"),
    EN_RIESGO("En riesgo");

    private final String descripcion;

    PerfilFinanciero(String descripcion) {
        this.descripcion = descripcion;
    }
}