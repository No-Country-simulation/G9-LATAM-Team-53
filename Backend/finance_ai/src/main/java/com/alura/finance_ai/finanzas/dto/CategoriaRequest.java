package com.alura.finance_ai.finanzas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank(message = "El nombre de la categoria no puede estar vacio")
        @Size(max = 255, message = "El nombre de la categoria no puede superar 255 caracteres")
        String nombre
) {
}
