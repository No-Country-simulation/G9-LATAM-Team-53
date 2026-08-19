package com.alura.finance_ai.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "El email no puede estar vacío")
        @Email(message = "Formato de email invalido")
        String email,

        @NotBlank(message = "La contraseña no puede estar vacia")
        String contrasena
) {
}