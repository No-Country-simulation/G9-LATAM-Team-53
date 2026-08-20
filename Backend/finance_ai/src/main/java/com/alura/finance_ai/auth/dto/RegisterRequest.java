package com.alura.finance_ai.auth.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "El nombre no puede estar vacio")
        String nombre,

        @NotBlank(message = "El apellido no puede estar vacio")
        String apellido,

        @NotBlank(message = "El email no puede estar vacio")
        @Email(message = "Formato de email invalido")
        String email,

        @NotBlank(message = "La contraseña no puede estar vacia")
        @Size(min = 6, message = "La contraseña debe contener al menos 6 caracteres")
        String contrasena
) {
}
